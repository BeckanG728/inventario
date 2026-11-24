package com.taller.seguridad.autenticacion.security;

import com.taller.seguridad.config.SeguridadProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final SeguridadProperties appConfig;

    public String generateToken(Long userId, String role, String username, long versionSesion) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("username", username)
                .claim("role", role)
                .claim("sv", versionSesion)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(appConfig.getJwtExpiration())))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Token sin subject");
        }
        return Long.valueOf(subject);
    }

    public long getExpirationSeconds() {
        return appConfig.getJwtExpiration();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(appConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }
}
