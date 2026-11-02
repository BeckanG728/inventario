package com.taller.reporte.enums;

import java.util.Locale;

public enum FormatoReporte {

    PDF("application/pdf", ".pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");

    private final String contentType;
    private final String extension;

    FormatoReporte(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    public static FormatoReporte from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El formato es obligatorio. Use pdf o excel");
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "pdf" -> PDF;
            case "excel", "xlsx" -> EXCEL;
            default -> throw new IllegalArgumentException("Formato no válido. Use pdf o excel");
        };
    }
}
