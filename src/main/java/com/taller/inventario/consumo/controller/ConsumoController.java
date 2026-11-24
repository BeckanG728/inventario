package com.taller.inventario.consumo.controller;

import com.taller.seguridad.api.ActorAutenticado;
import com.taller.inventario.consumo.dto.request.ConsumoRequest;
import com.taller.inventario.consumo.dto.response.ConsumoResponse;
import com.taller.inventario.consumo.dto.response.ProductoFrecuenteResponse;
import com.taller.inventario.consumo.service.ConsumoService;
import com.taller.inventario.api.impl.OperacionIdempotente;
import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consumos")
@RequiredArgsConstructor
public class ConsumoController {

    private final ConsumoService consumoService;

    private final OperacionIdempotente operacionIdempotente;

    @PostMapping
    @PreAuthorize("hasAnyRole('MECANICO', 'JEFE_TALLER')")
    public ResponseEntity<ConsumoResponse> registrar(@Valid @RequestBody ConsumoRequest request,
                                                     @AuthenticationPrincipal ActorAutenticado actor,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID clave) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(operacionIdempotente.ejecutar(actor.id(), "consumo", clave, request,
                        ConsumoResponse.class, () -> consumoService.registrar(request, actor.id())));
    }

    /** Atajo para el formulario: los 5 productos activos más consumidos en los últimos 90 días. */
    @GetMapping("/productos-frecuentes")
    @PreAuthorize("hasAnyRole('MECANICO', 'JEFE_TALLER')")
    public List<ProductoFrecuenteResponse> productosFrecuentes() {
        return consumoService.productosFrecuentes();
    }
}
