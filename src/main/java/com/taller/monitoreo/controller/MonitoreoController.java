package com.taller.monitoreo.controller;

import com.taller.compartido.EstadoStock;
import com.taller.compartido.dto.PaginaResponse;
import com.taller.monitoreo.dto.response.AlertaStock;
import com.taller.monitoreo.service.AlertaService;
import com.taller.monitoreo.dto.request.ExistenciaConsultaFiltros;
import com.taller.monitoreo.dto.response.ExistenciaConsultaResponse;
import com.taller.monitoreo.service.ExistenciaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoreo")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('JEFE_TALLER', 'MECANICO')")
public class MonitoreoController {

    private final AlertaService alertaService;
    private final ExistenciaService existenciaService;

    /** Vista de detección: productos en condición de bajo stock. */
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaStock>> alertas() {
        return ResponseEntity.ok(alertaService.generarAlertas());
    }

    /** Vista operativa: listado de existencias solo de productos activos. */
    @GetMapping("/existencias")
    public ResponseEntity<PaginaResponse<ExistenciaConsultaResponse>> existencias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) EstadoStock estadoStock) {
        ExistenciaConsultaFiltros filtros =
                new ExistenciaConsultaFiltros(page, size, nombre, categoria, proveedorId, estadoStock);
        return ResponseEntity.ok(PaginaResponse.from(existenciaService.listarActivos(filtros)));
    }
}
