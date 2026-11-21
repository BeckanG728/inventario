package com.taller.inventario.api;

import com.taller.inventario.movimiento.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Operaciones de preparación del historial de inventario para los datos ficticios del perfil
 * {@code dev}: consumos y movimientos con fechas pasadas y estados de stock arbitrarios.
 */
public interface PreparacionInventario {

    record ConsumoDemo(Instant fecha, String finalidad, String referencia, Long usuarioId) {
    }

    record MovimientoDemo(
            TipoMovimiento tipo,
            Instant fecha,
            Long productoId,
            Long usuarioId,
            Integer cantidad,
            Integer variacion,
            Integer stockAnterior,
            Integer stockPosterior,
            BigDecimal costoUnitario,
            String motivo,
            Long consumoId
    ) {
    }

    Long crearConsumo(ConsumoDemo consumo);

    void guardarMovimientos(List<MovimientoDemo> movimientos);
}
