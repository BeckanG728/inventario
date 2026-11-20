package com.taller.ratelimit.store;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Implementación en memoria, válida mientras la aplicación tenga una sola instancia. */
@Component
public class InMemoryRateLimitStore implements RateLimitStore {

    private static final long LIMPIEZA_CADA = 1000;

    private final Clock clock;
    private final ConcurrentHashMap<String, Ventana> ventanas = new ConcurrentHashMap<>();
    private final AtomicLong operaciones = new AtomicLong();

    public InMemoryRateLimitStore() {
        this(Clock.systemUTC());
    }

    InMemoryRateLimitStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Resultado incrementar(String clave, Duration ventana) {
        Instant ahora = clock.instant();
        AtomicInteger contador = new AtomicInteger();

        ventanas.compute(clave, (ignorado, actual) -> {
            if (actual == null || !ahora.isBefore(actual.expiraEn())) {
                contador.set(1);
                return new Ventana(ahora.plus(ventana), 1);
            }
            contador.set(actual.contador() + 1);
            return new Ventana(actual.expiraEn(), actual.contador() + 1);
        });

        limpiarVentanasVencidas(ahora);
        Ventana estado = ventanas.get(clave);
        long reintentar = Math.max(1, Duration.between(ahora, estado.expiraEn()).getSeconds());
        return new Resultado(contador.get(), reintentar);
    }

    private void limpiarVentanasVencidas(Instant ahora) {
        if (operaciones.incrementAndGet() % LIMPIEZA_CADA == 0) {
            ventanas.entrySet().removeIf(entrada -> !ahora.isBefore(entrada.getValue().expiraEn()));
        }
    }

    private record Ventana(Instant expiraEn, int contador) {
    }
}
