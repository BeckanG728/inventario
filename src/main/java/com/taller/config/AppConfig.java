package com.taller.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @PostConstruct
    public void validateEnvironment() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("FATAL: la variable JWT_SECRET es obligatoria y no puede estar vacía");
        }
        if (jwtExpiration == null || jwtExpiration <= 0) {
            throw new IllegalStateException("FATAL: la variable JWT_EXPIRACION_SEGUNDOS debe ser mayor que 0");
        }
        if (internalApiKey == null || internalApiKey.isBlank()) {
            throw new IllegalStateException("FATAL: la variable INTERNAL_API_KEY es obligatoria y no puede estar vacía");
        }
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public String getInternalApiKey() {
        return internalApiKey;
    }
}