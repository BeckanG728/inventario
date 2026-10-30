package com.taller.movimiento.controller;

import com.taller.auth.entity.Usuario;
import com.taller.movimiento.dto.request.AjusteRequest;
import com.taller.movimiento.dto.request.EntradaRequest;
import com.taller.movimiento.dto.response.EntradaResponse;
import com.taller.movimiento.dto.response.MovimientoResponse;
import com.taller.movimiento.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping("/ajuste")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<MovimientoResponse> ajuste(@Valid @RequestBody AjusteRequest request,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.ajuste(request, usuario));
    }

    @PostMapping("/entrada")
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<EntradaResponse> entrada(@Valid @RequestBody EntradaRequest request,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.entrada(request, usuario));
    }
}
