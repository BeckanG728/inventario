package com.taller.reportes.estadisticas.service;

import com.taller.reportes.estadisticas.enums.Granularity;
import java.time.LocalDate;
import java.util.Map;

public interface EstadisticasService {

    Map<LocalDate, Long> consultar(LocalDate from, LocalDate to, Granularity granularity);
}