package com.taller.reporte.service;

import com.taller.reporte.dto.response.ProveedorBajoStock;
import com.taller.reporte.dto.response.ReporteConsumoItem;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.enums.FormatoReporte;

import java.util.List;

public interface ReporteExportService {

    byte[] exportarMovimientos(ReporteMovimientosResponse reporte, FormatoReporte formato);

    byte[] exportarConsumo(List<ReporteConsumoItem> reporte, FormatoReporte formato);

    byte[] exportarBajoStock(List<ProveedorBajoStock> reporte, FormatoReporte formato);
}
