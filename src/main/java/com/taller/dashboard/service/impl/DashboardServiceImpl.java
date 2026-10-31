package com.taller.dashboard.service.impl;

import com.taller.alerta.dto.response.AlertaStock;
import com.taller.alerta.service.AlertaService;
import com.taller.dashboard.dto.response.DashboardResponse;
import com.taller.dashboard.dto.response.DashboardResponse.AlertasResumen;
import com.taller.dashboard.dto.response.DashboardResponse.InventarioResumen;
import com.taller.dashboard.dto.response.DashboardResponse.Kpis;
import com.taller.dashboard.dto.response.DashboardResponse.MovimientoReciente;
import com.taller.dashboard.repository.DashboardMovimientoRepository;
import com.taller.dashboard.repository.DashboardProductoRepository;
import com.taller.dashboard.service.DashboardService;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ValorInventarioResponse;
import com.taller.reporte.service.InventarioValorService;
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
    private final DashboardProductoRepository productoRepository;
    private final DashboardMovimientoRepository movimientoRepository;
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
                movimientoRepository.findTop5ByOrderByFechaDescIdDesc().stream()
                        .map(this::toMovimientoReciente)
                        .toList());
    }

    /** RN302: bajo stock = stock_actual <= stock_minimo (el stock 0 siempre cuenta). */
    private InventarioResumen resumirInventario(List<AlertaStock> alertas) {
        long total = productoRepository.countByActivoTrue();
        long bajoStock = alertas.size();
        long sinStock = alertas.stream().filter(a -> a.stockActual() == 0).count();
        return new InventarioResumen(total, Math.max(total - bajoStock, 0), bajoStock, sinStock);
    }

    private long contarMes(TipoMovimiento tipo, Instant inicioMes) {
        return movimientoRepository.countByTipoAndFechaGreaterThanEqual(tipo, inicioMes);
    }

    private MovimientoReciente toMovimientoReciente(MovimientoInventario movimiento) {
        return new MovimientoReciente(
                movimiento.getId(),
                movimiento.getFecha(),
                movimiento.getTipo(),
                movimiento.getProducto().getCodigo(),
                movimiento.getProducto().getNombre(),
                movimiento.getCantidad(),
                movimiento.getVariacion(),
                movimiento.getUsuario().getUsername());
    }
}