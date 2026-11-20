package com.taller.ratelimit.store;

import java.time.Duration;

/** Abstracción del contador de peticiones. Permite reemplazar el estado en memoria por un store compartido. */
public interface RateLimitStore {

    Resultado incrementar(String clave, Duration ventana);

    record Resultado(int contador, long reintentarEnSegundos) {
    }
}
