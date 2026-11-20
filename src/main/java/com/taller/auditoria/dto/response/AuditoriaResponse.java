package com.taller.auditoria.dto.response;

import com.taller.auditoria.enums.AuditoriaTipo;

import java.time.Instant;

/** Asiento de auditoría expuesto a la consulta de administración. */
public record AuditoriaResponse(
        Long id,
        Instant fecha,
        AuditoriaTipo tipo,
        Long actorId,
        String actorUsername,
        String metodo,
        String endpoint,
        String recurso,
        String recursoId,
        String resultado,
        boolean exito
) {
}
