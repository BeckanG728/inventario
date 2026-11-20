package com.taller.auditoria.listener;

import com.taller.auditoria.domain.AuditoriaOperacion;
import com.taller.auditoria.enums.AuditoriaTipo;
import com.taller.auditoria.event.OperacionAuditadaEvent;
import com.taller.auditoria.repository.AuditoriaOperacionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AuditoriaEventListenerTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T17:00:00Z");

    private final AuditoriaOperacionRepository repository = mock(AuditoriaOperacionRepository.class);
    private final Clock clock = Clock.fixed(AHORA, ZoneOffset.UTC);
    private final AuditoriaEventListener listener = new AuditoriaEventListener(repository, clock);

    private OperacionAuditadaEvent evento() {
        return new OperacionAuditadaEvent(
                AuditoriaTipo.OPERACION, 12L, "juan", "PUT", "/api/productos/3",
                "producto", "3", "OK", true);
    }

    @Test
    void registrar_guardaAsientoConFechaDelClock() {
        listener.registrar(evento());

        ArgumentCaptor<AuditoriaOperacion> captor = ArgumentCaptor.forClass(AuditoriaOperacion.class);
        verify(repository).save(captor.capture());
        AuditoriaOperacion asiento = captor.getValue();
        assertEquals(AHORA, asiento.getFecha());
        assertEquals(AuditoriaTipo.OPERACION, asiento.getTipo());
        assertEquals(12L, asiento.getActorId());
        assertEquals("juan", asiento.getActorUsername());
        assertEquals("PUT", asiento.getMetodo());
        assertEquals("/api/productos/3", asiento.getEndpoint());
        assertEquals("producto", asiento.getRecurso());
        assertEquals("3", asiento.getRecursoId());
        assertEquals("OK", asiento.getResultado());
        assertEquals(true, asiento.isExito());
    }

    @Test
    void registrar_eventoSinActorGuardaSistema() {
        OperacionAuditadaEvent evento = new OperacionAuditadaEvent(
                AuditoriaTipo.AUTENTICACION, null, "sistema", "POST", "/api/auth/login",
                "login", null, "INVALID_CREDENTIALS", false);

        listener.registrar(evento);

        ArgumentCaptor<AuditoriaOperacion> captor = ArgumentCaptor.forClass(AuditoriaOperacion.class);
        verify(repository).save(captor.capture());
        assertNull(captor.getValue().getActorId());
        assertEquals("sistema", captor.getValue().getActorUsername());
        assertEquals("INVALID_CREDENTIALS", captor.getValue().getResultado());
        assertEquals(false, captor.getValue().isExito());
    }

    @Test
    void registrar_falloAlAuditar_noLanzaExcepcion() {
        doThrow(new RuntimeException("BD caída")).when(repository).save(any(AuditoriaOperacion.class));

        assertDoesNotThrow(() -> listener.registrar(evento()));
    }
}
