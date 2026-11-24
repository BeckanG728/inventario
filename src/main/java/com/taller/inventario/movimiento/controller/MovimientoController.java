package com.taller.inventario.movimiento.controller;

import com.taller.inventario.movimiento.dto.request.AjusteRequest;
import com.taller.inventario.movimiento.dto.request.EntradaRequest;
import com.taller.inventario.movimiento.dto.response.EntradaResponse;
import com.taller.inventario.movimiento.dto.response.MovimientoResponse;
import com.taller.inventario.movimiento.dto.response.ReporteMovimientosPaginaResponse;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.service.MovimientoService;
import com.taller.inventario.movimiento.service.ReporteMovimientosService;
import com.taller.seguridad.api.ActorAutenticado;
import com.taller.inventario.api.impl.OperacionIdempotente;
import jakarta.validation.Valid;
import java.util.UUID;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JEFE_TALLER')")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final ReporteMovimientosService reporteMovimientosService;

    private final OperacionIdempotente operacionIdempotente;

    @PostMapping("/ajuste")
    public ResponseEntity<MovimientoResponse> ajuste(@Valid @RequestBody AjusteRequest request,
                                                     @AuthenticationPrincipal ActorAutenticado actor,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID clave) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(operacionIdempotente.ejecutar(actor.id(), "ajuste", clave, request,
                        MovimientoResponse.class, () -> movimientoService.ajuste(request, actor.id())));
    }

    @PostMapping("/entrada")
    public ResponseEntity<EntradaResponse> entrada(@Valid @RequestBody EntradaRequest request,
                                                   @AuthenticationPrincipal ActorAutenticado actor,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID clave) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(operacionIdempotente.ejecutar(actor.id(), "entrada", clave, request,
                        EntradaResponse.class, () -> movimientoService.entrada(request, actor.id())));
    }

    /** Reporte paginado de movimientos del dominio de inventario. */
    @GetMapping
    public ReporteMovimientosPaginaResponse consultar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reporteMovimientosService.consultarPagina(desde, hasta, tipo, page, size);
    }
}
