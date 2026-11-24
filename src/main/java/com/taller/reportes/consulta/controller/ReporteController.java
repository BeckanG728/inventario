package com.taller.reportes.consulta.controller;

import com.taller.reportes.consulta.service.InventarioValorService;
import com.taller.reportes.dto.response.ValorInventarioPaginaResponse;
import com.taller.reportes.exportacion.enums.FormatoReporte;
import com.taller.reportes.exportacion.service.ReporteExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JEFE_TALLER')")
public class ReporteController {

    private final InventarioValorService inventarioValorService;
    private final ReporteExportService reporteExportService;

    @GetMapping("/valor-inventario")
    public ValorInventarioPaginaResponse valorInventario(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return inventarioValorService.consultarPagina(page, size);
    }

    @GetMapping("/valor-inventario/exportar")
    public ResponseEntity<byte[]> exportarValorInventario(
            @RequestParam(required = false) String formato) {
        FormatoReporte formatoReporte = FormatoReporte.from(formato);
        byte[] contenido = reporteExportService.exportarValorInventario(
                inventarioValorService.consultar(), formatoReporte);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(formatoReporte.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte-valor-inventario" + formatoReporte.extension())
                .build());
        headers.setContentLength(contenido.length);
        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }
}
