package com.taller.reportes.dashboard.service.impl;

import com.taller.controlstock.alerta.dto.response.AlertaStock;
import com.taller.controlstock.alerta.service.AlertaService;
import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.reportes.dashboard.dto.response.DashboardResponse;
import com.taller.reportes.dashboard.dto.response.DashboardResponse.AlertasResumen;
import com.taller.reportes.dashboard.dto.response.DashboardResponse.InventarioResumen;
import com.taller.reportes.dashboard.dto.response.DashboardResponse.Kpis;
import com.taller.reportes.dashboard.dto.response.DashboardResponse.MovimientoReciente;
import com.taller.reportes.dashboard.service.DashboardService;
import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.reportes.dto.response.ValorInventarioResponse;
import com.taller.reportes.consulta.service.InventarioValorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int LIMITE_ALERTAS = 5;

    private final AlertaService alertaService;
    private final InventarioValorService inventarioValorService;
    private final ProductoStockMinimo productoStockMinimo;
    private final HistorialMovimientos historialMovimientos;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtener() {
        List<AlertaStock> alertas = alertaService.generarAlertas();
        ValorInventarioResponse valor = inventarioValorService.consultar();
        Instant inicioMes = YearMonth.now(clock).atDay(1).atStartOfDay(clock.getZone()).toInstant();

        return new DashboardResponse(
                resumirInventario(alertas),
                new Kpis(
                        valor.totalGeneral(),
                        valor.productosSinCosto(),
                        contarMes(TipoMovimiento.ENTRADA, inicioMes),
                        contarMes(TipoMovimiento.CONSUMO, inicioMes),
                        contarMes(TipoMovimiento.AJUSTE, inicioMes)),
                new AlertasResumen(alertas.size(), alertas.stream().limit(LIMITE_ALERTAS).toList()),
                historialMovimientos.consultarUltimos().stream()
                        .map(this::toMovimientoReciente)
                        .toList());
    }

    /** RN302: bajo stock = stock_actual <= stock_minimo (el stock 0 siempre cuenta). */
    private InventarioResumen resumirInventario(List<AlertaStock> alertas) {
        long total = productoStockMinimo.contarActivos();
        long bajoStock = alertas.size();
        long sinStock = alertas.stream().filter(a -> a.stockActual() == 0).count();
        return new InventarioResumen(total, Math.max(total - bajoStock, 0), bajoStock, sinStock);
    }

    private long contarMes(TipoMovimiento tipo, Instant inicioMes) {
        return historialMovimientos.contarPorTipoYFechaDesde(tipo, inicioMes);
    }

    private MovimientoReciente toMovimientoReciente(MovimientoDetalle movimiento) {
        return new MovimientoReciente(
                movimiento.id(),
                movimiento.fecha(),
                movimiento.tipo(),
                movimiento.productoCodigo(),
                movimiento.productoNombre(),
                movimiento.cantidad(),
                movimiento.variacion(),
                movimiento.usuarioUsername());
    }
}
