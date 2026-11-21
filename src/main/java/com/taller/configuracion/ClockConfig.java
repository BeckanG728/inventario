package com.taller.configuracion;

import com.taller.compartido.ZonaHorariaNegocio;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZonaHorariaNegocio.ZONA_NEGOCIO);
    }
}
