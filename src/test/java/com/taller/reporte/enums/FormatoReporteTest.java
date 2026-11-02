package com.taller.reporte.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * US-21 — Selección del formato de exportación.
 */
class FormatoReporteTest {

    @Test
    void from_pdf_enMinusculas() {
        assertEquals(FormatoReporte.PDF, FormatoReporte.from("pdf"));
    }

    @Test
    void from_pdf_enMayusculas() {
        assertEquals(FormatoReporte.PDF, FormatoReporte.from("PDF"));
    }

    @Test
    void from_excel() {
        assertEquals(FormatoReporte.EXCEL, FormatoReporte.from("excel"));
    }

    @Test
    void from_xlsx_comoAliasDeExcel() {
        assertEquals(FormatoReporte.EXCEL, FormatoReporte.from("xlsx"));
    }

    @Test
    void from_formatoVacio_lanzaError() {
        assertThrows(IllegalArgumentException.class, () -> FormatoReporte.from("  "));
    }

    @Test
    void from_formatoNulo_lanzaError() {
        assertThrows(IllegalArgumentException.class, () -> FormatoReporte.from(null));
    }

    @Test
    void from_formatoNoSoportado_lanzaErrorConMensajeClaro() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> FormatoReporte.from("word"));
        assertEquals("Formato no válido. Use pdf o excel", ex.getMessage());
    }
}
