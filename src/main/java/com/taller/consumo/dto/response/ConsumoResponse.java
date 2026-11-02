package com.taller.consumo.dto.response;

import com.taller.consumo.entity.Consumo;
import com.taller.movimiento.dto.response.MovimientoResponse;
import com.taller.movimiento.entity.MovimientoInventario;

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
