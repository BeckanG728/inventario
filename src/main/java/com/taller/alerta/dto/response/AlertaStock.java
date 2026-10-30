package com.taller.alerta.dto.response;

public record AlertaStock(
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer cantidadSugerida,
        String proveedor
) {
}