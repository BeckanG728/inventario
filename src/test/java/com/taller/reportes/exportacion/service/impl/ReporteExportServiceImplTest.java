package com.taller.reportes.exportacion.service.impl;

import com.taller.reportes.dto.response.ValorInventarioDetalle;
import com.taller.reportes.dto.response.ValorInventarioResponse;
import com.taller.reportes.exportacion.enums.FormatoReporte;
import com.taller.reportes.exportacion.export.ReporteExcelExporter;
import com.taller.reportes.exportacion.export.ReportePdfExporter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * US-21 — Exportación del reporte de valor de inventario a PDF y Excel.
 */
class ReporteExportServiceImplTest {

    private static final byte[] FIRMA_PDF = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private final ReporteExportServiceImpl service = new ReporteExportServiceImpl(
            new ReportePdfExporter(), new ReporteExcelExporter());

    private ValorInventarioResponse reporte() {
        ValorInventarioDetalle detalle = new ValorInventarioDetalle(
                "FIL-001", "Filtro Aceite", 20,
                new BigDecimal("8.50"), new BigDecimal("170.00"));
        return new ValorInventarioResponse(
                new BigDecimal("170.00"), 1, 0, List.of(detalle));
    }

    @Test
    void exportarValorInventario_pdf_generaArchivoPdf() {
        byte[] archivo = service.exportarValorInventario(reporte(), FormatoReporte.PDF);

        assertTrue(archivo.length > 0);
        assertTrue(contieneFirma(archivo, FIRMA_PDF));
    }

    @Test
    void exportarValorInventario_excel_generaArchivoXlsx() {
        byte[] archivo = service.exportarValorInventario(reporte(), FormatoReporte.EXCEL);

        assertTrue(archivo.length > 0);
        assertTrue(esZip(archivo));
    }

    @Test
    void exportarReporteVacio_tambienGeneraArchivosValidos() {
        ValorInventarioResponse vacio =
                new ValorInventarioResponse(BigDecimal.ZERO, 0, 0, List.of());

        byte[] pdf = service.exportarValorInventario(vacio, FormatoReporte.PDF);
        byte[] excel = service.exportarValorInventario(vacio, FormatoReporte.EXCEL);

        assertTrue(contieneFirma(pdf, FIRMA_PDF));
        assertTrue(esZip(excel));
    }

    private boolean contieneFirma(byte[] archivo, byte[] firma) {
        if (archivo.length < firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if (archivo[i] != firma[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean esZip(byte[] archivo) {
        return archivo.length > 2 && archivo[0] == 'P' && archivo[1] == 'K';
    }
}
