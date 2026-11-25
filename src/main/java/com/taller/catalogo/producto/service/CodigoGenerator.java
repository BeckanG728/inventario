package com.taller.catalogo.producto.service;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Genera el código de producto con el patrón {@code AAA-nnnn}: siglas del nombre de la categoría
 * y correlativo dentro de ese prefijo. El usuario nunca lo introduce ni lo edita.
 */
public final class CodigoGenerator {

    private CodigoGenerator() {
    }

    public static String generar(String prefijo, int correlativo) {
        return String.format(Locale.ROOT, "%s-%04d", prefijo, correlativo);
    }

    /** Tres primeras letras del nombre de la categoría, sin tildes y en mayúsculas; rellena con X si hace falta. */
    public static String prefijo(String categoriaNombre) {
        String limpio = Normalizer.normalize(categoriaNombre == null ? "" : categoriaNombre, Normalizer.Form.NFD)
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
