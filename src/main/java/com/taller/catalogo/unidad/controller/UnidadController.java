package com.taller.catalogo.unidad.controller;

import com.taller.catalogo.unidad.dto.response.UnidadResponse;
import com.taller.catalogo.unidad.service.UnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/unidades")
@RequiredArgsConstructor
public class UnidadController {

    private final UnidadService unidadService;

    /** Catálogo de unidades, accesible a todos los roles operativos. */
    @GetMapping
    @PreAuthorize("hasAnyRole('JEFE_TALLER', 'MECANICO', 'ADMIN')")
    public ResponseEntity<List<UnidadResponse>> listar() {
        return ResponseEntity.ok(unidadService.listarActivas());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnidadResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(unidadService.desactivar(id));
    }
}
