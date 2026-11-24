package com.taller.reportes.estadisticas.service;

import com.taller.compartido.ZonaHorariaNegocio;

import com.taller.reportes.estadisticas.enums.Granularity;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Lógica pura: cuenta movimientos por día, semana (lunes ISO) o mes (día 1). Zona: America/Lima. */
public final class EstadisticasBucketing {

    private EstadisticasBucketing() {
    }

    public static LocalDate bucket(Instant fecha, Granularity granularity) {
        LocalDate day = fecha.atZone(ZonaHorariaNegocio.ZONA_NEGOCIO).toLocalDate();
        return switch (granularity) {
            case DAY -> day;
            case WEEK -> day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> day.withDayOfMonth(1);
        };
    }

    /** Inicio del período (día, lunes ISO o día 1) al que pertenece {@code dia}. */
    public static LocalDate bucketDia(LocalDate dia, Granularity granularity) {
        return switch (granularity) {
            case DAY -> dia;
            case WEEK -> dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> dia.withDayOfMonth(1);
        };
    }

    /** Inicios de todos los períodos entre {@code from} y {@code to} (ambos inclusive), incluidos los vacíos. */
    public static List<LocalDate> periodos(LocalDate from, LocalDate to, Granularity granularity) {
        List<LocalDate> periodos = new ArrayList<>();
        LocalDate actual = bucketDia(from, granularity);
        while (!actual.isAfter(to)) {
            periodos.add(actual);
            actual = switch (granularity) {
                case DAY -> actual.plusDays(1);
                case WEEK -> actual.plusWeeks(1);
                case MONTH -> actual.plusMonths(1);
            };
        }
        return periodos;
    }

    public static Map<LocalDate, Long> agrupar(List<Instant> fechas, Granularity granularity) {
        return fechas.stream().collect(Collectors.groupingBy(
                f -> bucket(f, granularity), TreeMap::new, Collectors.counting()));
    }
}