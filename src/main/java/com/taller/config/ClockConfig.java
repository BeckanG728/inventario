package com.taller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    /** Zona horaria de negocio: los instantes se almacenan en UTC. */
    public static final String ZONA_NEGOCIO_ID = "America/Lima";
    public static final ZoneId ZONA_NEGOCIO = ZoneId.of(ZONA_NEGOCIO_ID);

    @Bean
    public Clock clock() {
        return Clock.system(ZONA_NEGOCIO);
    }
}