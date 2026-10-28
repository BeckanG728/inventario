package com.taller.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest {

    private static final String VALID_SECRET = "a".repeat(32);

    private AppConfig buildConfig(String secret, Long expiration) {
        AppConfig config = new AppConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", secret);
        ReflectionTestUtils.setField(config, "jwtExpiration", expiration);
        return config;
    }

    @Test
    void validateEnvironment_withAllValues_doesNotThrow() {
        AppConfig config = buildConfig(VALID_SECRET, 3600L);
        assertDoesNotThrow(config::validateEnvironment);
    }

    @Test
    void validateEnvironment_nullSecret_throws() {
        AppConfig config = buildConfig(null, 3600L);
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }

    @Test
    void validateEnvironment_blankSecret_throws() {
        AppConfig config = buildConfig("  ", 3600L);
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }

    @Test
    void validateEnvironment_shortSecret_throws() {
        AppConfig config = buildConfig("secret", 3600L);
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }

    @Test
    void validateEnvironment_zeroExpiration_throws() {
        AppConfig config = buildConfig(VALID_SECRET, 0L);
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }
}
