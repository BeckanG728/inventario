package com.taller.inventario.movimiento.dto.response;

import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimientoResponse(
        Long id,
        Instant fecha,
        TipoMovimiento tipo,
        Integer cantidad,
        Integer variacion,
        Integer stockAnterior,
        Integer stockPosterior,
        BigDecimal costoUnitario,
        String motivo,
        Long productoId,
        Long consumoId,
        Long usuarioId,
        String unidadRegistro,
        Integer factorAplicado,
        Integer cantidadBase
) {

    public static MovimientoResponse from(MovimientoInventario movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getTipo(),
                movimiento.getCantidad(),
                movimiento.getVariacion(),
                movimiento.getStockAnterior(),
                movimiento.getStockPosterior(),
                movimiento.getCostoUnitario(),
                movimiento.getMotivo(),
                movimiento.getProductoId(),
                movimiento.getConsumoId(),
                movimiento.getUsuarioId(),
                movimiento.getUnidadRegistro(),
                movimiento.getFactorAplicado(),
                movimiento.getCantidadBase());
    }

    public static MovimientoResponse from(MovimientoDetalle movimiento) {
        return new MovimientoResponse(
                movimiento.id(),
                movimiento.fecha(),
                movimiento.tipo(),
                movimiento.cantidad(),
                movimiento.variacion(),
                movimiento.stockAnterior(),
                movimiento.stockPosterior(),
                movimiento.costoUnitario(),
                movimiento.motivo(),
                movimiento.productoId(),
                movimiento.consumoId(),
                movimiento.usuarioId(),
                movimiento.unidadRegistro(),
                movimiento.factorAplicado(),
                movimiento.cantidadBase());
    }
}
