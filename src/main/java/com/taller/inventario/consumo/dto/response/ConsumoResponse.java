package com.taller.inventario.consumo.dto.response;

import com.taller.inventario.consumo.entity.Consumo;
import com.taller.inventario.movimiento.dto.response.MovimientoResponse;
import com.taller.inventario.movimiento.entity.MovimientoInventario;

import java.time.Instant;
import java.util.List;

public record ConsumoResponse(
        Long id,
        Instant fecha,
        String finalidad,
        String referenciaVehiculoCliente,
        Long usuarioId,
        List<MovimientoResponse> movimientos
) {

    public static ConsumoResponse from(Consumo consumo, List<MovimientoInventario> movimientos) {
        return new ConsumoResponse(
                consumo.getId(),
                consumo.getFecha(),
                consumo.getFinalidad(),
                consumo.getReferenciaVehiculoCliente(),
                consumo.getUsuario().getId(),
                movimientos.stream().map(MovimientoResponse::from).toList());
    }
}
