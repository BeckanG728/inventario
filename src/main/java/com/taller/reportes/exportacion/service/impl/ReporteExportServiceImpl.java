package com.taller.reportes.exportacion.service.impl;

import com.taller.reportes.dto.response.ValorInventarioDetalle;
import com.taller.reportes.dto.response.ValorInventarioResponse;
import com.taller.reportes.exportacion.enums.FormatoReporte;
import com.taller.reportes.exportacion.export.ReporteExcelExporter;
import com.taller.reportes.exportacion.export.ReportePdfExporter;
import com.taller.reportes.exportacion.export.ReporteTabla;
import com.taller.reportes.exportacion.service.ReporteExportService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporteExportServiceImpl implements ReporteExportService {

    private static final List<String> COLUMNAS = List.of(
            "Código", "Producto", "Stock actual", "Costo última compra", "Valor estimado");

    private final ReportePdfExporter pdfExporter;
    private final ReporteExcelExporter excelExporter;

    @Override
    public byte[] exportarValorInventario(ValorInventarioResponse reporte, FormatoReporte formato) {
        String subtitulo = "Total: " + valor(reporte.totalGeneral())
                + " | Con costo: " + reporte.totalProductosConCosto()
                + " | Sin costo: " + reporte.productosSinCosto();

        List<List<String>> filas = reporte.detalle().stream()
                .map(this::fila)
                .toList();

        ReporteTabla tabla = new ReporteTabla(
                "Reporte de valor de inventario", subtitulo, COLUMNAS, filas);
        return switch (formato) {
            case PDF -> pdfExporter.generar(tabla);
            case EXCEL -> excelExporter.generar(tabla);
        };
    }

    private List<String> fila(ValorInventarioDetalle detalle) {
        return List.of(
                valor(detalle.codigo()),
                valor(detalle.nombre()),
                valor(detalle.stockActual()),
                valor(detalle.costoUltimaCompra()),
                valor(detalle.valorEstimadoProducto()));
    }

    private String valor(Object valor) {
        return valor == null ? "" : valor instanceof BigDecimal decimal ? decimal.toPlainString() : valor.toString();
    }
}
