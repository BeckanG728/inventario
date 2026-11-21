package com.taller.seguridad.ratelimit.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Parámetros de limitación. Todos se proveen por variables de entorno, sin defaults. */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    @Min(1)
    private int loginIp;

    @Min(1)
    private int loginUsername;

    @Min(1)
    private int usuario;

    @Min(1)
    private int swagger;

    @Min(1)
    private int ventanaLoginSegundos;

    @Min(1)
    private int ventanaUsuarioSegundos;
}
