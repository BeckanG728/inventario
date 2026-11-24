package com.taller.catalogo.producto.service;

import java.util.Locale;

/**
 * Genera el código de producto con el patrón {@code PRD-nnnn}: un correlativo global
 * asignado por el backend. El usuario nunca lo introduce ni lo edita.
 */
public final class CodigoGenerator {

    private CodigoGenerator() {
    }

    public static String generar(int correlativo) {
        return String.format(Locale.ROOT, "PRD-%04d", correlativo);
    }

    /** Últimos cuatro dígitos del código; 0 si no se puede interpretar. */
    public static int correlativoDe(String codigo) {
        if (codigo == null) {
            return 0;
        }
        int guion = codigo.lastIndexOf('-');
        if (guion < 0 || guion + 1 >= codigo.length()) {
            return 0;
        }
        try {
            return Integer.parseInt(codigo.substring(guion + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
