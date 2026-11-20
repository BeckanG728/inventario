package com.taller.common.error;

import com.taller.config.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/** Único creador del cuerpo de error, con el reloj de la aplicación. */
@Component
@RequiredArgsConstructor
public class ErrorResponseFactory {

    private final Clock clock;

    public ErrorResponse crear(ErrorDefinition definition, String endpoint) {
        return new ErrorResponse(
                definition.code(),
                definition.message(),
                definition.details(),
                endpoint,
                Instant.now(clock));
    }
}
