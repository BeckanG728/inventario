package com.taller.reporte.controller;

import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.service.ReporteMovimientosService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteMovimientosController {

    private final ReporteMovimientosService reporteMovimientosService;

    @GetMapping("/movimientos")
    public ReporteMovimientosResponse movimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo) {
        return reporteMovimientosService.consultar(desde, hasta, tipo);
    }
}
