package com.taller.inventario.api;

import com.taller.inventario.movimiento.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Movimiento con los datos públicos de producto y usuario ya resueltos.
 *
 * <p>Se obtiene en lote para evitar consultas N+1 y para que los dominios consumidores no
 * necesiten entidades ni repositorios ajenos.</p>
 */
public record MovimientoDetalle(
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
        String productoCodigo,
        String productoNombre,
        Long consumoId,
        Long usuarioId,
        String usuarioUsername,
        String unidadRegistro,
        Integer factorAplicado,
        Integer cantidadBase
) {
}
