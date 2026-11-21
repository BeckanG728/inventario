package com.taller.reportes.dto.response;

public record ProductoBajoStock(
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer sugerido
) {
}