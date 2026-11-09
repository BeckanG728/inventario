package com.taller.reporte.service.impl;

import com.taller.inventario.movimiento.dto.response.MovimientoResponse;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ProductoBajoStock;
import com.taller.reporte.dto.response.ProveedorBajoStock;
import com.taller.reporte.dto.response.ReporteConsumoItem;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.enums.FormatoReporte;
import com.taller.reporte.export.ReporteExcelExporter;
import com.taller.reporte.export.ReportePdfExporter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * US-21 — Exportación de reportes a PDF y Excel.
 */
class ReporteExportServiceImplTest {

    private static final byte[] FIRMA_PDF = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private final ReporteExportServiceImpl service = new ReporteExportServiceImpl(
            new ReportePdfExporter(), new ReporteExcelExporter());

    private ReporteMovimientosResponse reporteMovimientos() {
        MovimientoResponse movimiento = new MovimientoResponse(
                1L, Instant.parse("2026-01-10T10:00:00Z"), TipoMovimiento.ENTRADA,
                5, null, 0, 5, new BigDecimal("12.50"), "Compra", 1L, null, 2L);
        return new ReporteMovimientosResponse(1, 0, 0, List.of(movimiento));
    }

    private List<ReporteConsumoItem> reporteConsumo() {
        return List.of(new ReporteConsumoItem(
                1L, "FILT-01", "Filtro Aceite", "juan",
                List.of("Cambio de aceite"), 3,
                List.of(Instant.parse("2026-01-10T10:00:00Z"))));
    }

    private List<ProveedorBajoStock> reporteBajoStock() {
        return List.of(new ProveedorBajoStock("Distribuidor Los Pinos", List.of(
                new ProductoBajoStock("FILT-01", "Filtro Aceite", 2, 10, 8))));
    }

    @Test
    void exportarMovimientos_pdf_generaArchivoPdf() {
        byte[] archivo = service.exportarMovimientos(reporteMovimientos(), FormatoReporte.PDF);

        assertTrue(archivo.length > 0);
        assertTrue(contieneFirma(archivo, FIRMA_PDF));
    }

    @Test
    void exportarMovimientos_excel_generaArchivoXlsx() {
        byte[] archivo = service.exportarMovimientos(reporteMovimientos(), FormatoReporte.EXCEL);

        assertTrue(archivo.length > 0);
        assertTrue(esZip(archivo));
    }

    @Test
    void exportarConsumo_pdf_generaArchivoPdf() {
        byte[] archivo = service.exportarConsumo(reporteConsumo(), FormatoReporte.PDF);

        assertTrue(archivo.length > 0);
        assertTrue(contieneFirma(archivo, FIRMA_PDF));
    }

    @Test
    void exportarConsumo_excel_generaArchivoXlsx() {
        byte[] archivo = service.exportarConsumo(reporteConsumo(), FormatoReporte.EXCEL);

        assertTrue(archivo.length > 0);
        assertTrue(esZip(archivo));
    }

    @Test
    void exportarBajoStock_pdf_generaArchivoPdf() {
        byte[] archivo = service.exportarBajoStock(reporteBajoStock(), FormatoReporte.PDF);

        assertTrue(archivo.length > 0);
        assertTrue(contieneFirma(archivo, FIRMA_PDF));
    }

    @Test
    void exportarBajoStock_excel_generaArchivoXlsx() {
        byte[] archivo = service.exportarBajoStock(reporteBajoStock(), FormatoReporte.EXCEL);

        assertTrue(archivo.length > 0);
        assertTrue(esZip(archivo));
    }

    @Test
    void exportarReportesVacios_tambienGeneraArchivosValidos() {
        ReporteMovimientosResponse vacio = new ReporteMovimientosResponse(0, 0, 0, List.of());

        byte[] pdf = service.exportarMovimientos(vacio, FormatoReporte.PDF);
        byte[] excel = service.exportarConsumo(List.of(), FormatoReporte.EXCEL);

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
