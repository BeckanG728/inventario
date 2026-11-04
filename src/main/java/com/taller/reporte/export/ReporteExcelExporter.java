package com.taller.reporte.export;

import com.taller.config.ClockConfig;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReporteExcelExporter {

    public byte[] generar(ReporteTabla tabla) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(nombreHoja(tabla.titulo()));
            CellStyle tituloStyle = crearEstiloTitulo(workbook);
            CellStyle subtituloStyle = crearEstiloSubtitulo(workbook);
            CellStyle encabezadoStyle = crearEstiloEncabezado(workbook);
            CellStyle celdaStyle = crearEstiloCelda(workbook);

            int columnas = Math.max(tabla.columnas().size(), 1);
            int filaActual = 0;

            Row filaTitulo = sheet.createRow(filaActual++);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue(tabla.titulo());
            celdaTitulo.setCellStyle(tituloStyle);
            combinarCeldas(sheet, filaTitulo.getRowNum(), columnas);

            if (tabla.subtitulo() != null && !tabla.subtitulo().isBlank()) {
                Row filaSubtitulo = sheet.createRow(filaActual++);
                Cell celdaSubtitulo = filaSubtitulo.createCell(0);
                celdaSubtitulo.setCellValue(tabla.subtitulo());
                celdaSubtitulo.setCellStyle(subtituloStyle);
                combinarCeldas(sheet, filaSubtitulo.getRowNum(), columnas);
            }

            String generado = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ClockConfig.ZONA_NEGOCIO)
                    .format(Instant.now());
            Row filaFecha = sheet.createRow(filaActual++);
            Cell celdaFecha = filaFecha.createCell(0);
            celdaFecha.setCellValue("Generado: " + generado);
            celdaFecha.setCellStyle(subtituloStyle);
            combinarCeldas(sheet, filaFecha.getRowNum(), columnas);

            Row filaEncabezado = sheet.createRow(filaActual++);
            for (int i = 0; i < tabla.columnas().size(); i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(tabla.columnas().get(i));
                celda.setCellStyle(encabezadoStyle);
            }

            if (tabla.filas().isEmpty()) {
                Row filaVacia = sheet.createRow(filaActual);
                Cell celdaVacia = filaVacia.createCell(0);
                celdaVacia.setCellValue("Sin datos para mostrar");
                celdaVacia.setCellStyle(celdaStyle);
                combinarCeldas(sheet, filaVacia.getRowNum(), columnas);
            } else {
                for (List<String> datos : tabla.filas()) {
                    Row fila = sheet.createRow(filaActual++);
                    for (int i = 0; i < tabla.columnas().size(); i++) {
                        Cell celda = fila.createCell(i);
                        celda.setCellValue(i < datos.size() && datos.get(i) != null ? datos.get(i) : "");
                        celda.setCellStyle(celdaStyle);
                    }
                }
            }

            for (int i = 0; i < tabla.columnas().size(); i++) {
                sheet.autoSizeColumn(i);
                int ancho = Math.min(sheet.getColumnWidth(i) + 512, 60 * 256);
                sheet.setColumnWidth(i, ancho);
            }

            workbook.write(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", ex);
        }
    }

    private void combinarCeldas(Sheet sheet, int fila, int columnas) {
        if (columnas > 1) {
            sheet.addMergedRegion(new CellRangeAddress(fila, fila, 0, columnas - 1));
        }
    }

    private String nombreHoja(String titulo) {
        String nombre = titulo == null ? "Reporte" : titulo.replaceAll("[\\\\/*?:\\[\\]]", " ").trim();
        if (nombre.isBlank()) {
            nombre = "Reporte";
        }
        return nombre.length() > 31 ? nombre.substring(0, 31) : nombre;
    }

    private CellStyle crearEstiloTitulo(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle crearEstiloSubtitulo(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloCelda(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBorderTop(BorderStyle.HAIR);
        style.setBorderLeft(BorderStyle.HAIR);
        style.setBorderRight(BorderStyle.HAIR);
        return style;
    }
}
