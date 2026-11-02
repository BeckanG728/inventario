package com.taller.reporte.export;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportePdfExporter {

    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD, Color.DARK_GRAY);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);
    private static final Font ENCABEZADO = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);
    private static final Font CELDA = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Color FONDO_ENCABEZADO = new Color(226, 232, 240);

    public byte[] generar(ReporteTabla tabla) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);

        try {
            PdfWriter.getInstance(documento, salida);
            documento.addTitle(tabla.titulo());
            documento.open();

            Paragraph titulo = new Paragraph(tabla.titulo(), TITULO);
            titulo.setAlignment(Element.ALIGN_LEFT);
            titulo.setSpacingAfter(4);
            documento.add(titulo);

            if (tabla.subtitulo() != null && !tabla.subtitulo().isBlank()) {
                Paragraph subtitulo = new Paragraph(tabla.subtitulo(), SUBTITULO);
                subtitulo.setSpacingAfter(2);
                documento.add(subtitulo);
            }

            String generado = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now());
            Paragraph fecha = new Paragraph("Generado: " + generado, SUBTITULO);
            fecha.setSpacingAfter(10);
            documento.add(fecha);

            documento.add(crearTabla(tabla));
        } finally {
            documento.close();
        }

        return salida.toByteArray();
    }

    private PdfPTable crearTabla(ReporteTabla tabla) {
        PdfPTable table = new PdfPTable(tabla.columnas().size());
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        for (String columna : tabla.columnas()) {
            PdfPCell celda = new PdfPCell(new Phrase(columna, ENCABEZADO));
            celda.setBackgroundColor(FONDO_ENCABEZADO);
            celda.setPadding(4);
            table.addCell(celda);
        }

        if (tabla.filas().isEmpty()) {
            PdfPCell vacia = new PdfPCell(new Phrase("Sin datos para mostrar", CELDA));
            vacia.setColspan(tabla.columnas().size());
            vacia.setPadding(6);
            table.addCell(vacia);
            return table;
        }

        for (List<String> fila : tabla.filas()) {
            for (int i = 0; i < tabla.columnas().size(); i++) {
                String valor = i < fila.size() ? fila.get(i) : "";
                PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "" : valor, CELDA));
                celda.setPadding(4);
                table.addCell(celda);
            }
        }
        return table;
    }
}
