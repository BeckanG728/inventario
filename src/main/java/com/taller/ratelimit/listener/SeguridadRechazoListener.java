package com.taller.ratelimit.listener;

import com.taller.ratelimit.event.SeguridadRechazoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Registra los rechazos como eventos de seguridad, sin escribir asientos en la tabla de auditoría. */
@Slf4j
@Component
public class SeguridadRechazoListener {

    @EventListener
    public void registrar(SeguridadRechazoEvent event) {
        log.warn("Rechazo por límite [{}] en {} (ip={}, usuario={}); reintentar en {} s",
                event.limite(), event.endpoint(), event.ip(), event.username(), event.reintentarEnSegundos());
    }
}
