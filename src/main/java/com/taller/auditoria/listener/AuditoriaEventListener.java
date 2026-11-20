package com.taller.auditoria.listener;

import com.taller.auditoria.domain.AuditoriaOperacion;
import com.taller.auditoria.event.OperacionAuditadaEvent;
import com.taller.auditoria.repository.AuditoriaOperacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * Persiste los eventos de auditoría después de que el interceptor haya terminado la petición,
 * es decir, cuando la operación de negocio ya fue confirmada (o revertida). Un fallo al
 * auditar se registra en el log pero nunca altera la respuesta ni revierte la operación.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaEventListener {

    private final AuditoriaOperacionRepository repository;
    private final Clock clock;

    @EventListener
    public void registrar(OperacionAuditadaEvent event) {
        try {
            repository.save(new AuditoriaOperacion(
                    Instant.now(clock),
                    event.tipo(),
                    event.actorId(),
                    event.actorUsername(),
                    event.metodo(),
                    event.endpoint(),
                    event.recurso(),
                    event.recursoId(),
                    event.resultado(),
                    event.exito()));
        } catch (Exception ex) {
            log.warn("No se pudo persistir la auditoría de {} {}: {}",
                    event.metodo(), event.endpoint(), ex.getMessage());
        }
    }
}
