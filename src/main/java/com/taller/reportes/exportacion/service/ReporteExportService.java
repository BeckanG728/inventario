package com.taller.reportes.exportacion.service;

import com.taller.reportes.dto.response.ProveedorBajoStock;
import com.taller.reportes.dto.response.ReporteConsumoItem;
import com.taller.reportes.dto.response.ReporteMovimientosResponse;
import com.taller.reportes.exportacion.enums.FormatoReporte;

import java.util.List;

public interface ReporteExportService {

    byte[] exportarMovimientos(ReporteMovimientosResponse reporte, FormatoReporte formato);

    byte[] exportarConsumo(List<ReporteConsumoItem> reporte, FormatoReporte formato);

    byte[] exportarBajoStock(List<ProveedorBajoStock> reporte, FormatoReporte formato);
}
