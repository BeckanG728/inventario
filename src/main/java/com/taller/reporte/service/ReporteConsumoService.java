package com.taller.reporte.service;

import com.taller.reporte.dto.response.ReporteConsumoItem;

import java.time.LocalDate;
import java.util.List;

public interface ReporteConsumoService {

    List<ReporteConsumoItem> consultar(LocalDate desde, LocalDate hasta);
}
