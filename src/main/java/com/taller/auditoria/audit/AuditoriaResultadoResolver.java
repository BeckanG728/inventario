package com.taller.auditoria.audit;

import com.taller.compartido.error.ErrorPolicyResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Traduce el resultado de la petición al código guardado en la auditoría. */
@Component
@RequiredArgsConstructor
public class AuditoriaResultadoResolver {

    private final ErrorPolicyResolver errorPolicyResolver;

    public String resolver(Throwable ex, int status) {
        return errorPolicyResolver.resolveAuditCode(ex, status);
    }
}
