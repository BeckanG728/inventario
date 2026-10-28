package com.taller.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Getter
@Configuration
public class AppConfig {

    static final int MIN_JWT_SECRET_BYTES = 32;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void validateEnvironment() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("FATAL: la variable JWT_SECRET es obligatoria y no puede estar vacía");
        }
        int secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8).length;
        if (secretBytes < MIN_JWT_SECRET_BYTES) {
            throw new IllegalStateException(
                    "FATAL: JWT_SECRET debe tener al menos " + MIN_JWT_SECRET_BYTES + " bytes para HS256");
        }
        if (jwtExpiration == null || jwtExpiration <= 0) {
            throw new IllegalStateException("FATAL: la variable JWT_EXPIRACION_SEGUNDOS debe ser mayor que 0");
        }
    }
}
