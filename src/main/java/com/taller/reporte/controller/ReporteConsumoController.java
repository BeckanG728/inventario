package com.taller.reporte.controller;

import com.taller.reporte.dto.response.ReporteConsumoItem;
import com.taller.reporte.service.ReporteConsumoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteConsumoController {

    private final ReporteConsumoService reporteConsumoService;

    @GetMapping("/consumo")
    public List<ReporteConsumoItem> consumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return reporteConsumoService.consultar(desde, hasta);
    }
}
