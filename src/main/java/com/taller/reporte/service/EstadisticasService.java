package com.taller.reporte.service;

import com.taller.reporte.enums.Granularity;
import java.time.LocalDate;
import java.util.Map;

public interface EstadisticasService {

    Map<LocalDate, Long> consultar(LocalDate from, LocalDate to, Granularity granularity);
}