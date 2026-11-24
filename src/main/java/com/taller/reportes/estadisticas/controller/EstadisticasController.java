package com.taller.reportes.estadisticas.controller;

import com.taller.reportes.dto.response.EstadisticasTipoPunto;
import com.taller.reportes.estadisticas.enums.Granularity;
import com.taller.reportes.estadisticas.service.EstadisticasService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JEFE_TALLER')")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    @GetMapping
    public Map<LocalDate, Long> consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Granularity granularity) {
        return estadisticasService.consultar(from, to, granularity);
    }

    /** Unidades por período y tipo; los períodos sin movimientos aparecen en cero. */
    @GetMapping("/por-tipo")
    public List<EstadisticasTipoPunto> consultarPorTipo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Granularity granularity,
            @RequestParam(required = false) Long productoId) {
        return estadisticasService.consultarPorTipo(from, to, granularity, productoId);
    }
}