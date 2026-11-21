package com.taller.seguridad.ratelimit.filter;

import com.taller.compartido.error.ErrorCode;
import com.taller.compartido.error.ErrorDefinition;
import com.taller.seguridad.autenticacion.security.ErrorResponseWriter;
import com.taller.seguridad.api.ActorAutenticado;
import com.taller.seguridad.ratelimit.config.RateLimitProperties;
import com.taller.seguridad.ratelimit.event.SeguridadRechazoEvent;
import com.taller.seguridad.ratelimit.store.RateLimitStore;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioRateLimitFilterTest {

    private static final Duration VENTANA = Duration.ofSeconds(60);

    private final RateLimitStore store = mock(RateLimitStore.class);
    private final ErrorResponseWriter writer = mock(ErrorResponseWriter.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final RateLimitProperties properties = new RateLimitProperties();

    private UsuarioRateLimitFilter filtro;

    @BeforeEach
    void setUp() {
        properties.setUsuario(2);
        properties.setSwagger(5);
        properties.setVentanaUsuarioSegundos(60);
        filtro = new UsuarioRateLimitFilter(store, properties, writer, publisher);
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar() {
        ActorAutenticado actor = new ActorAutenticado(
                42L, "juan", List.of(new SimpleGrantedAuthority("ROLE_MECANICO")));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(actor, null, actor.authorities()));
    }

    private MockHttpServletRequest peticion(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setRequestURI(uri);
        request.setRemoteAddr("10.0.0.1");
        return request;
    }

    @Test
    void dentroDelLimite_pasaLaPeticion() throws Exception {
        autenticar();
        when(store.incrementar("usuario:42", VENTANA))
                .thenReturn(new RateLimitStore.Resultado(1, 60));
        boolean[] paso = {false};

        filtro.doFilter(peticion("/api/productos"), new MockHttpServletResponse(),
                (request, response) -> paso[0] = true);

        assertTrue(paso[0]);
        verify(writer, never()).write(any(), any(), any());
    }

    @Test
    void excedeLimitePorUsuario_devuelve429YPublicaEvento() throws Exception {
        autenticar();
        when(store.incrementar("usuario:42", VENTANA))
                .thenReturn(new RateLimitStore.Resultado(3, 25));
        boolean[] paso = {false};
        MockHttpServletRequest request = peticion("/api/productos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filtro.doFilter(request, response, (req, res) -> paso[0] = true);

        assertFalse(paso[0]);
        assertEquals("25", response.getHeader("Retry-After"));
        verify(writer).write(request, response, ErrorDefinition.of(ErrorCode.RATE_LIMIT_EXCEEDED));

        ArgumentCaptor<SeguridadRechazoEvent> captor = ArgumentCaptor.forClass(SeguridadRechazoEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals("USUARIO", captor.getValue().limite());
        assertEquals("juan", captor.getValue().username());
        assertEquals(25, captor.getValue().reintentarEnSegundos());
    }

    @Test
    void swaggerUsaElLimiteHolgado() throws Exception {
        autenticar();
        when(store.incrementar("usuario:42", VENTANA))
                .thenReturn(new RateLimitStore.Resultado(4, 60));
        boolean[] paso = {false};

        filtro.doFilter(peticion("/v3/api-docs"), new MockHttpServletResponse(),
                (request, response) -> paso[0] = true);

        assertTrue(paso[0]);
    }

    @Test
    void sinAutenticacionNoSeCuenta() throws Exception {
        boolean[] paso = {false};

        filtro.doFilter(peticion("/api/productos"), new MockHttpServletResponse(),
                (request, response) -> paso[0] = true);

        assertTrue(paso[0]);
        verify(store, never()).incrementar(any(), any());
    }

    @Test
    void healthErrorYLoginSeExcluyen() throws Exception {
        autenticar();

        for (String uri : new String[]{"/actuator/health", "/error", "/api/auth/login"}) {
            filtro.doFilter(peticion(uri), new MockHttpServletResponse(), (request, response) -> {
            });
        }

        verify(store, never()).incrementar(any(), any());
    }
}
