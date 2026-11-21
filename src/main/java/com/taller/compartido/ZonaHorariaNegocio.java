package com.taller.compartido;

import java.time.ZoneId;

/** Zona horaria de negocio compartida. Los instantes se almacenan en UTC. */
public final class ZonaHorariaNegocio {

    public static final String ZONA_NEGOCIO_ID = "America/Lima";
    public static final ZoneId ZONA_NEGOCIO = ZoneId.of(ZONA_NEGOCIO_ID);

    private ZonaHorariaNegocio() {
    }
}
