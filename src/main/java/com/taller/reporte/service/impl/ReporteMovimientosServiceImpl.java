package com.taller.reporte.service.impl;

import com.taller.movimiento.dto.response.MovimientoResponse;
import com.taller.movimiento.entity.MovimientoInventario;
import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.repository.MovimientoReporteRepository;
import com.taller.reporte.service.ReporteMovimientosService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteMovimientosServiceImpl implements ReporteMovimientosService {

    private final MovimientoReporteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ReporteMovimientosResponse consultar(LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Los parámetros desde y hasta son obligatorios");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }

        ZoneId zone = ZoneId.systemDefault();
        Instant start = desde.atStartOfDay(zone).toInstant();
        Instant endExclusive = hasta.plusDays(1).atStartOfDay(zone).toInstant(); // 'hasta' inclusive

        List<MovimientoInventario> movimientos = tipo == null
                ? repository.findMovimientosBetween(start, endExclusive)
                : repository.findMovimientosBetweenAndTipo(start, endExclusive, tipo);

        return new ReporteMovimientosResponse(
                contar(movimientos, TipoMovimiento.ENTRADA),
                contar(movimientos, TipoMovimiento.CONSUMO),
                contar(movimientos, TipoMovimiento.AJUSTE),
                movimientos.stream().map(MovimientoResponse::from).toList());
    }

    private long contar(List<MovimientoInventario> movimientos, TipoMovimiento tipo) {
        return movimientos.stream().filter(m -> m.getTipo() == tipo).count();
    }
}
