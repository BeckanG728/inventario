package com.taller.reporte.controller;

import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ProveedorBajoStock;
import com.taller.reporte.dto.response.ReporteConsumoItem;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.enums.FormatoReporte;
import com.taller.reporte.service.ReporteBajoStockService;
import com.taller.reporte.service.ReporteConsumoService;
import com.taller.reporte.service.ReporteExportService;
import com.taller.reporte.service.ReporteMovimientosService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteExportController {

    private final ReporteMovimientosService reporteMovimientosService;
    private final ReporteConsumoService reporteConsumoService;
    private final ReporteBajoStockService reporteBajoStockService;
    private final ReporteExportService reporteExportService;

    @GetMapping("/movimientos/exportar")
    public ResponseEntity<byte[]> exportarMovimientos(
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo) {

        FormatoReporte formatoReporte = FormatoReporte.from(formato);
        ReporteMovimientosResponse reporte = reporteMovimientosService.consultar(desde, hasta, tipo);
        byte[] contenido = reporteExportService.exportarMovimientos(reporte, formatoReporte);
        return archivo(contenido, formatoReporte, "reporte-movimientos");
    }

    @GetMapping("/consumo/exportar")
    public ResponseEntity<byte[]> exportarConsumo(
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        FormatoReporte formatoReporte = FormatoReporte.from(formato);
        List<ReporteConsumoItem> reporte = reporteConsumoService.consultar(desde, hasta);
        byte[] contenido = reporteExportService.exportarConsumo(reporte, formatoReporte);
        return archivo(contenido, formatoReporte, "reporte-consumo");
    }

    @GetMapping("/bajo-stock/exportar")
    public ResponseEntity<byte[]> exportarBajoStock(@RequestParam(required = false) String formato) {
        FormatoReporte formatoReporte = FormatoReporte.from(formato);
        List<ProveedorBajoStock> reporte = reporteBajoStockService.consultar();
        byte[] contenido = reporteExportService.exportarBajoStock(reporte, formatoReporte);
        return archivo(contenido, formatoReporte, "reporte-bajo-stock");
    }

    private ResponseEntity<byte[]> archivo(byte[] contenido, FormatoReporte formato, String nombre) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(formato.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(nombre + formato.extension())
                .build());
        headers.setContentLength(contenido.length);
        return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
    }
}
