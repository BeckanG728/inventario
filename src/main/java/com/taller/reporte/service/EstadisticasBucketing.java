package com.taller.reporte.service;

import com.taller.reporte.enums.Granularity;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Lógica pura: cuenta movimientos por día, semana (lunes ISO) o mes (día 1). Zona: UTC. */
public final class EstadisticasBucketing {

    private EstadisticasBucketing() {
    }

    public static LocalDate bucket(Instant fecha, Granularity granularity) {
        LocalDate day = fecha.atZone(ZoneOffset.UTC).toLocalDate();
        return switch (granularity) {
            case DAY -> day;
            case WEEK -> day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> day.withDayOfMonth(1);
        };
    }

    public static Map<LocalDate, Long> agrupar(List<Instant> fechas, Granularity granularity) {
        return fechas.stream().collect(Collectors.groupingBy(
                f -> bucket(f, granularity), TreeMap::new, Collectors.counting()));
    }
}