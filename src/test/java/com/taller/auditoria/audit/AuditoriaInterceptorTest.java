package com.taller.auditoria.audit;

import com.taller.auth.entity.Rol;
import com.taller.auth.entity.Usuario;
import com.taller.auditoria.enums.AuditoriaTipo;
import com.taller.auditoria.event.OperacionAuditadaEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditoriaInterceptorTest {

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final AuditoriaInterceptor interceptor = new AuditoriaInterceptor(publisher);

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private Usuario usuarioAutenticado() {
        Usuario usuario = new Usuario("mecanico", "pwd", new Rol("MECANICO"));
        usuario.setId(42L);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities()));
        return usuario;
    }

    private MockHttpServletRequest peticion(String metodo, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, uri);
        request.setRequestURI(uri);
        return request;
    }

    @Test
    void postAutenticado_publicaEventoConSnapshotDelActor() {
        usuarioAutenticado();
        MockHttpServletRequest request = peticion("POST", "/api/productos/7/ajustes");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        assertTrue(interceptor.preHandle(request, response, new Object()));
        interceptor.afterCompletion(request, response, new Object(), null);

        ArgumentCaptor<OperacionAuditadaEvent> captor = ArgumentCaptor.forClass(OperacionAuditadaEvent.class);
        verify(publisher).publishEvent(captor.capture());
        OperacionAuditadaEvent event = captor.getValue();
        assertEquals(AuditoriaTipo.OPERACION, event.tipo());
        assertEquals(42L, event.actorId());
        assertEquals("mecanico", event.actorUsername());
        assertEquals("POST", event.metodo());
        assertEquals("/api/productos/7/ajustes", event.endpoint());
        assertEquals("producto", event.recurso());
        assertEquals("7", event.recursoId());
        assertEquals("OK", event.resultado());
        assertTrue(event.exito());
    }

    @Test
    void peticionFallida_publicaElCodigoDeError() {
        usuarioAutenticado();
        MockHttpServletRequest request = peticion("POST", "/api/productos");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(400);

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), new IllegalArgumentException("Inválido"));

        ArgumentCaptor<OperacionAuditadaEvent> captor = ArgumentCaptor.forClass(OperacionAuditadaEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertEquals("VALIDATION_ERROR", captor.getValue().resultado());
        assertFalse(captor.getValue().exito());
    }

    @Test
    void loginSinAutenticacion_seRegistraComoSeguridadYSistema() {
        MockHttpServletRequest request = peticion("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        interceptor.preHandle(request, response, new Object());
        interceptor.afterCompletion(request, response, new Object(), null);

        ArgumentCaptor<OperacionAuditadaEvent> captor = ArgumentCaptor.forClass(OperacionAuditadaEvent.class);
        verify(publisher).publishEvent(captor.capture());
        OperacionAuditadaEvent event = captor.getValue();
        assertEquals(AuditoriaTipo.AUTENTICACION, event.tipo());
        assertNull(event.actorId());
        assertEquals("sistema", event.actorUsername());
        assertEquals("login", event.recurso());
    }

    @Test
    void consultasYAuditoriaNoSeAuditan() {
        MockHttpServletRequest get = peticion("GET", "/api/productos");
        MockHttpServletRequest auditoria = peticion("POST", "/api/auditoria");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(get, response, new Object()));
        assertTrue(interceptor.preHandle(auditoria, response, new Object()));
        interceptor.afterCompletion(get, response, new Object(), null);
        interceptor.afterCompletion(auditoria, response, new Object(), null);

        verify(publisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void endpointsFueraDeApiNoSeAuditan() {
        MockHttpServletRequest request = peticion("POST", "/actuator/shutdown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        interceptor.afterCompletion(request, response, new Object(), null);

        verify(publisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }
}
