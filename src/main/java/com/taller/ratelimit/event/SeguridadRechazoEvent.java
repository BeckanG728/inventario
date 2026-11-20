package com.taller.ratelimit.event;

/** Evento de seguridad para rechazos de limitación. No se persiste en la auditoría de operaciones. */
public record SeguridadRechazoEvent(
        String limite,
        String ip,
        String username,
        String endpoint,
        long reintentarEnSegundos) {
}
