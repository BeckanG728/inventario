package com.taller.reportes.estadisticas.service;

import com.taller.reportes.dto.response.EstadisticasTipoPunto;
import com.taller.reportes.estadisticas.enums.Granularity;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EstadisticasService {

    Map<LocalDate, Long> consultar(LocalDate from, LocalDate to, Granularity granularity);

    /** Unidades por período y tipo; opcionalmente filtradas por producto. */
    List<EstadisticasTipoPunto> consultarPorTipo(LocalDate from, LocalDate to, Granularity granularity,
                                                 Long productoId);
}