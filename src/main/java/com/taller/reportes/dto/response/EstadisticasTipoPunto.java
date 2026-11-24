package com.taller.reportes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Unidades y costos movilizados en un período (inicio del período en {@code fecha}), separados por tipo.
 * Los costos son la suma de {@code cantidad × costoUnitario} de cada movimiento del período.
 */
public record EstadisticasTipoPunto(
        LocalDate fecha,
        long entrada,
        long consumo,
        long ajuste,
        BigDecimal costoEntradas,
        BigDecimal costoConsumos,
        BigDecimal costoAjustes
) {
}
