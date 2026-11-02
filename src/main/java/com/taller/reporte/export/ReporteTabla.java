package com.taller.reporte.export;

import java.util.List;

/**
 * Representación tabular común para exportar un reporte a PDF o Excel.
 */
public record ReporteTabla(
        String titulo,
        String subtitulo,
        List<String> columnas,
        List<List<String>> filas
) {
}
