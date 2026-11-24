package com.taller.seguridad.autenticacion.security;

import com.taller.seguridad.config.SeguridadProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(appConfig("a".repeat(32), 3600L));
    }

    @Test
    void generateToken_includesSubjectUsernameAndRole() {
        String token = jwtUtil.generateToken(7L, "MECANICO", "carlos", 0L);
        Claims claims = jwtUtil.parse(token);

        assertEquals("7", claims.getSubject());
        assertEquals("carlos", claims.get("username", String.class));
        assertEquals("MECANICO", claims.get("role", String.class));
        assertEquals(7L, jwtUtil.extractUserId(claims));
        assertEquals(3600L, jwtUtil.getExpirationSeconds());
    }

    @Test
    void parse_withWrongSecret_fails() {
        String token = jwtUtil.generateToken(1L, "MECANICO", "a", 0L);
        JwtUtil other = new JwtUtil(appConfig("b".repeat(32), 3600L));

        assertThrows(JwtException.class, () -> other.parse(token));
    }

    @Test
    void parse_expiredToken_throwsExpiredJwtException() {
        JwtUtil shortLived = new JwtUtil(appConfig("a".repeat(32), -1L));
        String token = shortLived.generateToken(1L, "MECANICO", "a", 0L);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.parse(token));
    }

    @Test
    void extractUserId_blankSubject_throws() {
        Claims claims = io.jsonwebtoken.Jwts.claims().subject("  ").build();
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.extractUserId(claims));
    }

    private static SeguridadProperties appConfig(String secret, long expiration) {
        SeguridadProperties config = new SeguridadProperties();
        ReflectionTestUtils.setField(config, "jwtSecret", secret);
        ReflectionTestUtils.setField(config, "jwtExpiration", expiration);
        return config;
    }
}
