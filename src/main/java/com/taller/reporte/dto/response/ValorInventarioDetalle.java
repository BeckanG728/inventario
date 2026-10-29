package com.taller.reporte.dto.response;

import java.math.BigDecimal;

public record ValorInventarioDetalle(String codigo, String nombre, int stockActual,
                                     BigDecimal costoUltimaCompra, BigDecimal valorEstimadoProducto) {
}