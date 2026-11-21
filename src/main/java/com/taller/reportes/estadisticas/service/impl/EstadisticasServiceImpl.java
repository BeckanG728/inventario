package com.taller.reportes.estadisticas.service.impl;

import com.taller.compartido.ZonaHorariaNegocio;

import com.taller.inventario.api.HistorialMovimientos;
import com.taller.reportes.estadisticas.enums.Granularity;
import com.taller.reportes.estadisticas.service.EstadisticasBucketing;
import com.taller.reportes.estadisticas.service.EstadisticasService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstadisticasServiceImpl implements EstadisticasService {

    private final HistorialMovimientos historialMovimientos;

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
        Instant start = from.atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO).toInstant();
        Instant endExclusive = to.plusDays(1).atStartOfDay(ZonaHorariaNegocio.ZONA_NEGOCIO).toInstant(); // 'to' inclusive
        List<Instant> fechas = historialMovimientos.consultarFechasEntre(start, endExclusive);
        return EstadisticasBucketing.agrupar(fechas, granularity);
    }
}