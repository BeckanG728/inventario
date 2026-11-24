package com.taller.inventario.movimiento.service.impl;

import com.taller.compartido.ZonaHorariaNegocio;

import com.taller.inventario.api.HistorialMovimientos;
import com.taller.inventario.api.MovimientoDetalle;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.dto.response.MovimientoReporteResponse;
import com.taller.inventario.movimiento.dto.response.ReporteMovimientosResponse;
import com.taller.inventario.movimiento.dto.response.ReporteMovimientosPaginaResponse;
import com.taller.inventario.movimiento.service.ReporteMovimientosService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    private final HistorialMovimientos historialMovimientos;

    @Override
    @Transactional(readOnly = true)
    public ReporteMovimientosResponse consultar(LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
        validarRango(desde, hasta);
        Instant start = inicio(desde);
        Instant endExclusive = finExclusivo(hasta);

        List<MovimientoDetalle> movimientos =
                historialMovimientos.consultarEntre(start, endExclusive, tipo);

        return new ReporteMovimientosResponse(
                contar(movimientos, TipoMovimiento.ENTRADA),
                contar(movimientos, TipoMovimiento.CONSUMO),
                contar(movimientos, TipoMovimiento.AJUSTE),
                movimientos.stream().map(MovimientoReporteResponse::from).toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteMovimientosPaginaResponse consultarPagina(LocalDate desde, LocalDate hasta, TipoMovimiento tipo,
                                                            int page, int size) {
        validarRango(desde, hasta);
        Instant start = inicio(desde);
        Instant endExclusive = finExclusivo(hasta);

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO));
        Page<MovimientoDetalle> pagina =
                historialMovimientos.consultarPagina(start, endExclusive, tipo, pageable);
        Map<TipoMovimiento, Long> totales =
                historialMovimientos.contarPorTipoEntre(start, endExclusive);

        return new ReporteMovimientosPaginaResponse(
                totalDeTipo(totales, TipoMovimiento.ENTRADA, tipo),
                totalDeTipo(totales, TipoMovimiento.CONSUMO, tipo),
                totalDeTipo(totales, TipoMovimiento.AJUSTE, tipo),
                pagina.getContent().stream().map(MovimientoReporteResponse::from).toList(),
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
        return desde.atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO).toInstant();
    }

    private Instant finExclusivo(LocalDate hasta) {
        return hasta.plusDays(1).atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO).toInstant(); // 'hasta' inclusive
    }

    /** Total del tipo indicado; si hay filtro por tipo, los demás tipos cuentan 0. */
    private long totalDeTipo(Map<TipoMovimiento, Long> totales, TipoMovimiento tipo, TipoMovimiento filtro) {
        if (filtro != null && filtro != tipo) {
            return 0;
        }
        return totales.getOrDefault(tipo, 0L);
    }

    private long contar(List<MovimientoDetalle> movimientos, TipoMovimiento tipo) {
        return movimientos.stream().filter(m -> m.tipo() == tipo).count();
    }
}
