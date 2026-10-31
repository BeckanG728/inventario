package com.taller.dashboard.dto.response;

import com.taller.alerta.dto.response.AlertaStock;
import com.taller.movimiento.enums.TipoMovimiento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DashboardResponse(
        InventarioResumen inventario,
        Kpis kpis,
        AlertasResumen alertas,
        List<MovimientoReciente> ultimosMovimientos
) {

    public record InventarioResumen(
            long totalProductos,
            long productosStockNormal,
            long productosBajoStock,
            long productosSinStock
    ) {
    }

    public record Kpis(
            BigDecimal valorEstimadoInventario,
            int productosSinCosto,
            long entradasMes,
            long consumosMes,
            long ajustesMes
    ) {
    }

    public record AlertasResumen(
            int total,
            List<AlertaStock> masUrgentes
    ) {
    }

    public record MovimientoReciente(
            Long id,
            Instant fecha,
            TipoMovimiento tipo,
            String productoCodigo,
            String productoNombre,
            Integer cantidad,
            Integer variacion,
            String usuario
    ) {
    }
}