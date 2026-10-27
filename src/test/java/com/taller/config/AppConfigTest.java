package com.taller.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AppConfigTest {

    private AppConfig buildConfig(String secret, Long expiration, String apiKey) {
        AppConfig config = new AppConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", secret);
        ReflectionTestUtils.setField(config, "jwtExpiration", expiration);
        ReflectionTestUtils.setField(config, "internalApiKey", apiKey);
        return config;
    }

    @Test
    void validateEnvironment_withAllValues_doesNotThrow() {
        AppConfig config = buildConfig("secret", 3600L, "key");
        assertDoesNotThrow(config::validateEnvironment);
    }

    @Test
    void validateEnvironment_nullSecret_throws() {
        AppConfig config = buildConfig(null, 3600L, "key");
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }

    @Test
    void validateEnvironment_blankSecret_throws() {
        AppConfig config = buildConfig("  ", 3600L, "key");
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }

    @Test
    void validateEnvironment_zeroExpiration_throws() {
        AppConfig config = buildConfig("secret", 0L, "key");
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }

    @Test
    void validateEnvironment_blankApiKey_throws() {
        AppConfig config = buildConfig("secret", 3600L, "");
        assertThrows(IllegalStateException.class, config::validateEnvironment);
    }
}