package com.taller.ratelimit.store;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryRateLimitStoreTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T18:00:00Z");
    private static final Duration VENTANA = Duration.ofSeconds(60);

    @Test
    void incrementaDentroDeLaMismaVentana() {
        InMemoryRateLimitStore store = new InMemoryRateLimitStore(Clock.fixed(AHORA, ZoneOffset.UTC));

        RateLimitStore.Resultado primero = store.incrementar("login:ip:1.2.3.4", VENTANA);
        RateLimitStore.Resultado segundo = store.incrementar("login:ip:1.2.3.4", VENTANA);

        assertEquals(1, primero.contador());
        assertEquals(2, segundo.contador());
        assertEquals(60, segundo.reintentarEnSegundos());
    }

    @Test
    void claveDistintaMantieneSuPropioContador() {
        InMemoryRateLimitStore store = new InMemoryRateLimitStore(Clock.fixed(AHORA, ZoneOffset.UTC));

        store.incrementar("login:ip:1.2.3.4", VENTANA);
        RateLimitStore.Resultado otra = store.incrementar("login:ip:5.6.7.8", VENTANA);

        assertEquals(1, otra.contador());
    }

    @Test
    void vencidaLaVentanaElContadorSeReinicia() {
        InMemoryRateLimitStore store = new InMemoryRateLimitStore(Clock.fixed(AHORA, ZoneOffset.UTC));
        store.incrementar("login:ip:1.2.3.4", VENTANA);
        store.incrementar("login:ip:1.2.3.4", VENTANA);

        InMemoryRateLimitStore despues = new InMemoryRateLimitStore(
                Clock.fixed(AHORA.plusSeconds(61), ZoneOffset.UTC));
        RateLimitStore.Resultado reiniciado = despues.incrementar("login:ip:1.2.3.4", VENTANA);

        assertEquals(1, reiniciado.contador());
    }
}
