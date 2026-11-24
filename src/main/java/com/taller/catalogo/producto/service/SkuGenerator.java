package com.taller.catalogo.producto.service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Genera el SKU con el patrón {@code AAA-ccc-nnnn}: siglas del producto, id de categoría
 * y correlativo dentro de ese prefijo. El correlativo garantiza la unicidad sin depender
 * de atributos mutables como el proveedor.
 */
public final class SkuGenerator {

    private SkuGenerator() {
    }

    public static String generar(String nombre, Long categoriaId, int correlativo) {
        return String.format(Locale.ROOT, "%s-%03d-%04d", siglas(nombre), categoriaId, correlativo);
    }

    public static String prefijo(String nombre, Long categoriaId) {
        return String.format(Locale.ROOT, "%s-%03d", siglas(nombre), categoriaId);
    }

    /** Últimos cuatro dígitos del SKU; 0 si no se puede interpretar. */
    public static int correlativoDe(String sku) {
        if (sku == null) {
            return 0;
        }
        int ultimoGuion = sku.lastIndexOf('-');
        if (ultimoGuion < 0 || ultimoGuion + 1 >= sku.length()) {
            return 0;
        }
        try {
            return Integer.parseInt(sku.substring(ultimoGuion + 1));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Tres primeras letras del nombre, sin tildes y en mayúsculas; rellena con X si hace falta. */
    private static String siglas(String nombre) {
        String limpio = Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        StringBuilder resultado = new StringBuilder(3);
        for (int i = 0; i < limpio.length() && resultado.length() < 3; i++) {
            char caracter = limpio.charAt(i);
            if (Character.isLetter(caracter)) {
                resultado.append(Character.toUpperCase(caracter));
            }
        }
        while (resultado.length() < 3) {
            resultado.append('X');
        }
        return resultado.toString();
    }
}
