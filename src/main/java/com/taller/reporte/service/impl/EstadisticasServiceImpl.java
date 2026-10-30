package com.taller.reporte.service.impl;

import com.taller.reporte.enums.Granularity;
import com.taller.reporte.repository.MovimientoReporteRepository;
import com.taller.reporte.service.EstadisticasBucketing;
import com.taller.reporte.service.EstadisticasService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements EstadisticasService {

    private final MovimientoReporteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> consultar(LocalDate from, LocalDate to, Granularity granularity) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Los parámetros from y to son obligatorios");
        }
        if (granularity == null) {
            throw new IllegalArgumentException("El parámetro granularity es obligatorio (DAY, WEEK o MONTH)");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }
        Instant start = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(); // 'to' inclusive
        List<Instant> fechas = repository.findFechasBetween(start, endExclusive);
        return EstadisticasBucketing.agrupar(fechas, granularity);
    }
}