package com.taller.sec.service;

import com.taller.config.AppConfig;
import com.taller.sec.entity.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AppConfig appConfig;

    public JwtService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("username", usuario.getUsername())
                .claim("role", usuario.getRol().getNombre())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(appConfig.getJwtExpiration())))
                .signWith(signingKey(), Jwts.SIG.HS256)
                .compact();
    }

    public long getExpirationSeconds() {
        return appConfig.getJwtExpiration();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(appConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }
}