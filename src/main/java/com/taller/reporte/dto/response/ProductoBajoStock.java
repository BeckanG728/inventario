package com.taller.reporte.dto.response;

public record ProductoBajoStock(
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer sugerido
) {
}