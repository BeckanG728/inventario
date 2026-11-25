package com.taller.catalogo.categoria.controller;

import com.taller.catalogo.categoria.dto.request.CategoriaRequest;
import com.taller.catalogo.categoria.dto.response.CategoriaResponse;
import com.taller.catalogo.categoria.service.CategoriaService;
import com.taller.catalogo.unidad.dto.response.UnidadResponse;
import com.taller.catalogo.unidad.service.UnidadService;
import com.taller.compartido.dto.PaginaResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final UnidadService unidadService;

    @PostMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('JEFE_TALLER')")
    public ResponseEntity<PaginaResponse<CategoriaResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(PaginaResponse.from(categoriaService.listar(page, size, nombre, activo)));
    }

    /** Listado ligero para los combobox de filtros; accesible también al mecánico. */
    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('JEFE_TALLER', 'MECANICO')")
    public ResponseEntity<List<CategoriaResponse>> listarActivos() {
        return ResponseEntity.ok(categoriaService.listarActivos());
    }

    /** Unidades válidas para una categoría; alimenta el combobox del formulario de producto. */
    @GetMapping("/{id}/unidades")
    @PreAuthorize("hasAnyRole('JEFE_TALLER', 'MECANICO')")
    public ResponseEntity<List<UnidadResponse>> listarUnidades(@PathVariable Long id) {
        return ResponseEntity.ok(unidadService.listarPorCategoria(id));
    }
}