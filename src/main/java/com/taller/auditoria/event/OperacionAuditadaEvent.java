package com.taller.auditoria.event;

import com.taller.auditoria.enums.AuditoriaTipo;

public record OperacionAuditadaEvent(
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
