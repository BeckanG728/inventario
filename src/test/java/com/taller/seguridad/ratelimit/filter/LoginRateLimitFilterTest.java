package com.taller.seguridad.ratelimit.filter;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.seguridad.autenticacion.security.ErrorResponseWriter;
import com.taller.seguridad.ratelimit.config.RateLimitProperties;
import com.taller.seguridad.ratelimit.event.SeguridadRechazoEvent;
import com.taller.seguridad.ratelimit.store.RateLimitStore;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginRateLimitFilterTest {

    private static final Duration VENTANA = Duration.ofSeconds(60);

    private final RateLimitStore store = mock(RateLimitStore.class);
    private final ErrorResponseWriter writer = mock(ErrorResponseWriter.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final RateLimitProperties properties = new RateLimitProperties();

    private LoginRateLimitFilter filtro;

    @BeforeEach
    void setUp() throws Exception {
        properties.setLoginIp(5);
        properties.setLoginUsername(10);
        properties.setVentanaLoginSegundos(60);
        filtro = new LoginRateLimitFilter(store, properties, writer, publisher, objectMapper);

        JsonNode username = mock(JsonNode.class);
        when(username.asText(null)).thenReturn("juan");
        JsonNode raiz = mock(JsonNode.class);
        when(raiz.isObject()).thenReturn(true);
        when(raiz.path("username")).thenReturn(username);
        when(objectMapper.readTree(any(byte[].class))).thenReturn(raiz);
    }

    private MockHttpServletRequest login() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("10.0.0.1");
        request.setContent("{\"username\":\"juan\",\"password\":\"secreto\"}".getBytes(StandardCharsets.UTF_8));
        return request;
    }

    private void resultado(String clave, int contador, long reintentar) {
        when(store.incrementar(clave, VENTANA))
                .thenReturn(new RateLimitStore.Resultado(contador, reintentar));
    }

    @Test
    void dentroDelLimite_pasaYElCuerpoSigueDisponible() throws Exception {
        resultado("login:ip:10.0.0.1", 1, 60);
        resultado("login:user:juan", 1, 60);
        boolean[] paso = {false};

        FilterChain chain = (request, response) -> {
            paso[0] = true;
            String cuerpo = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("{\"username\":\"juan\",\"password\":\"secreto\"}", cuerpo);
        };
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(login(), response, chain);

        assertTrue(paso[0]);
        verify(writer, never()).write(any(), any(), any());
    }

    @Test
    void excedeLimitePorIp_devuelve429ConRetryAfterYEvento() throws Exception {
        resultado("login:ip:10.0.0.1", 6, 45);
        resultado("login:user:juan", 1, 60);
        boolean[] paso = {false};
        FilterChain chain = (request, response) -> paso[0] = true;
        MockHttpServletRequest request = login();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, chain);

        assertFalse(paso[0]);
        assertEquals("45", response.getHeader("Retry-After"));
        verify(writer).write(request, response, ErrorDefinition.of(ErrorCode.RATE_LIMIT_EXCEEDED));

        ArgumentCaptor<SeguridadRechazoEvent> captor = ArgumentCaptor.forClass(SeguridadRechazoEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals("LOGIN_IP", captor.getValue().limite());
        assertEquals("10.0.0.1", captor.getValue().ip());
        assertEquals("juan", captor.getValue().username());
        assertEquals(45, captor.getValue().reintentarEnSegundos());
    }

    @Test
    void excedeLimitePorUsername_devuelve429() throws Exception {
        resultado("login:ip:10.0.0.1", 1, 60);
        resultado("login:user:juan", 11, 30);
        MockHttpServletRequest request = login();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> {
        });

        assertEquals("30", response.getHeader("Retry-After"));
        ArgumentCaptor<SeguridadRechazoEvent> captor = ArgumentCaptor.forClass(SeguridadRechazoEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals("LOGIN_USERNAME", captor.getValue().limite());
    }

    @Test
    void sinUsername_soloAplicaElLimitePorIp() throws Exception {
        JsonNode raiz = mock(JsonNode.class);
        when(raiz.isObject()).thenReturn(true);
        when(raiz.path("username")).thenReturn(mock(JsonNode.class));
        when(objectMapper.readTree(any(byte[].class))).thenReturn(raiz);
        resultado("login:ip:10.0.0.1", 1, 60);

        boolean[] paso = {false};
        filtro.doFilter(login(), new MockHttpServletResponse(), (request, response) -> paso[0] = true);

        assertTrue(paso[0]);
        verify(store).incrementar("login:ip:10.0.0.1", VENTANA);
        verify(store, never()).incrementar(org.mockito.ArgumentMatchers.startsWith("login:user:"), any());
    }

    @Test
    void peticionDistintaALoginNoSeCuenta() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/productos");
        request.setRequestURI("/api/productos");
        boolean[] paso = {false};

        filtro.doFilter(request, new MockHttpServletResponse(), (req, res) -> paso[0] = true);

        assertTrue(paso[0]);
        verify(store, never()).incrementar(any(), any());
    }
}
