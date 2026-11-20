package com.taller.common.error;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/** Aplica las políticas registradas en orden y resuelve el fallback cuando ninguna reconoce el error. */
@Component
@RequiredArgsConstructor
public class ErrorPolicyResolver {

    private final List<ErrorPolicy> policies;

    public ErrorDefinition resolveMvc(Exception error) {
        return resolve(error, HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_ERROR.name());
    }

    public String resolveAuditCode(Throwable error, int status) {
        if (error == null && status < 400) {
            return "OK";
        }
        String fallbackCode = status >= 400 ? "HTTP_" + status : "ERROR";
        return resolve(error, status, fallbackCode).code();
    }

    private ErrorDefinition resolve(Throwable error, int fallbackStatus, String fallbackCode) {
        if (error != null) {
            for (ErrorPolicy policy : policies) {
                if (policy.supports(error)) {
                    return policy.resolve(error);
                }
            }
        }
        HttpStatus status = HttpStatus.resolve(fallbackStatus);
        return new ErrorDefinition(
                status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status,
                fallbackCode,
                "Error no previsto",
                List.of(),
                new HttpHeaders());
    }
}
