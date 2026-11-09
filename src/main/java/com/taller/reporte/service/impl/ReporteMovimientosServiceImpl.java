package com.taller.reporte.service.impl;

import com.taller.config.ClockConfig;
import com.taller.inventario.movimiento.dto.response.MovimientoResponse;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.dto.response.ReporteMovimientosPaginaResponse;
import com.taller.reporte.repository.MovimientoReporteRepository;
import com.taller.reporte.service.ReporteMovimientosService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteMovimientosServiceImpl implements ReporteMovimientosService {

    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final MovimientoReporteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ReporteMovimientosResponse consultar(LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
        validarRango(desde, hasta);
        Instant start = inicio(desde);
        Instant endExclusive = finExclusivo(hasta);

        List<MovimientoInventario> movimientos = tipo == null
                ? repository.findMovimientosBetween(start, endExclusive)
                : repository.findMovimientosBetweenAndTipo(start, endExclusive, tipo);

        return new ReporteMovimientosResponse(
                contar(movimientos, TipoMovimiento.ENTRADA),
                contar(movimientos, TipoMovimiento.CONSUMO),
                contar(movimientos, TipoMovimiento.AJUSTE),
                movimientos.stream().map(MovimientoResponse::from).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteMovimientosPaginaResponse consultarPagina(LocalDate desde, LocalDate hasta, TipoMovimiento tipo,
                                                            int page, int size) {
        validarRango(desde, hasta);
        Instant start = inicio(desde);
        Instant endExclusive = finExclusivo(hasta);

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO));
        Page<MovimientoInventario> pagina = tipo == null
                ? repository.findPaginaBetween(start, endExclusive, pageable)
                : repository.findPaginaBetweenAndTipo(start, endExclusive, tipo, pageable);

        List<MovimientoReporteRepository.TotalPorTipo> totales = repository.contarPorTipoBetween(start, endExclusive);

        return new ReporteMovimientosPaginaResponse(
                totalDeTipo(totales, TipoMovimiento.ENTRADA, tipo),
                totalDeTipo(totales, TipoMovimiento.CONSUMO, tipo),
                totalDeTipo(totales, TipoMovimiento.AJUSTE, tipo),
                pagina.getContent().stream().map(MovimientoResponse::from).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parámetros desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
    }

    private Instant inicio(LocalDate desde) {
        return desde.atStartOfDay(ClockConfig.ZONA_NEGOCIO).toInstant();
    }

    private Instant finExclusivo(LocalDate hasta) {
        return hasta.plusDays(1).atStartOfDay(ClockConfig.ZONA_NEGOCIO).toInstant(); // 'hasta' inclusive
    }

    /** Total del tipo indicado; si hay filtro por tipo, los demás tipos cuentan 0. */
    private long totalDeTipo(List<MovimientoReporteRepository.TotalPorTipo> totales, TipoMovimiento tipo,
                             TipoMovimiento filtro) {
        if (filtro != null && filtro != tipo) {
            return 0;
        }
        return totales.stream().filter(t -> t.getTipo() == tipo).mapToLong(MovimientoReporteRepository.TotalPorTipo::getTotal)
                .sum();
    }

    private long contar(List<MovimientoInventario> movimientos, TipoMovimiento tipo) {
        return movimientos.stream().filter(m -> m.getTipo() == tipo).count();
    }
}
