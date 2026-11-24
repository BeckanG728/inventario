package com.taller.reportes.exportacion.service;

import com.taller.reportes.dto.response.ValorInventarioResponse;
import com.taller.reportes.exportacion.enums.FormatoReporte;

public interface ReporteExportService {

    byte[] exportarValorInventario(ValorInventarioResponse reporte, FormatoReporte formato);
}
