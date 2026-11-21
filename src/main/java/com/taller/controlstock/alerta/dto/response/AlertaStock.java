package com.taller.controlstock.alerta.dto.response;

public record AlertaStock(
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer cantidadSugerida,
        String proveedor
) {
}