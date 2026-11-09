package com.taller.seed.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "seed.demo")
@Data
public class DemoSeedProperties {

    private boolean enabled;
    private int categorias;
    private int proveedores;
    private int productos;
    private int entradas;
    private int consumos;
    private int ajustes;
    private long semilla;
    private String password;
    private Usuarios usuarios;

    @Data
    public static class Usuarios {
        private String jefe;
        private String mecanicos;
    }
}
