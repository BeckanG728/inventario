package com.taller.reportes.consulta.service;

import com.taller.reportes.dto.response.ReporteConsumoItem;

import java.time.LocalDate;
import java.util.List;

public interface ReporteConsumoService {

    List<ReporteConsumoItem> consultar(LocalDate desde, LocalDate hasta);
}
