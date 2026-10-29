package com.taller.reporte.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ValorInventarioResponse(BigDecimal totalGeneral, int totalProductosConCosto,
                                      int productosSinCosto, List<ValorInventarioDetalle> detalle) {
}