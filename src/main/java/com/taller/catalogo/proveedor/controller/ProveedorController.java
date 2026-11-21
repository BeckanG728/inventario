package com.taller.catalogo.proveedor.controller;

import com.taller.catalogo.proveedor.dto.request.ProveedorRequest;
import com.taller.catalogo.proveedor.dto.response.ProveedorResponse;
import com.taller.catalogo.proveedor.service.ProveedorService;
import com.taller.compartido.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<PaginaResponse<ProveedorResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String razonSocial,
            @RequestParam(required = false) String ruc,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(PaginaResponse.from(
                proveedorService.listar(page, size, razonSocial, ruc, activo)));
    }
}