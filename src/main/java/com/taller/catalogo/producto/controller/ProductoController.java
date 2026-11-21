package com.taller.catalogo.producto.controller;

import com.taller.catalogo.producto.dto.request.CreateProductoRequest;
import com.taller.catalogo.producto.dto.request.ProductoFiltro;
import com.taller.catalogo.producto.dto.request.UpdateProductoRequest;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.service.ProductoService;
import com.taller.compartido.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody CreateProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<PaginaResponse<ProductoResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long categoria,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Long proveedor,
            @RequestParam(required = false) String unidadMedida,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Boolean stockCritico) {
        ProductoFiltro filtro = new ProductoFiltro(
                nombre, categoria, codigo, proveedor, unidadMedida, activo, stockCritico);
        return ResponseEntity.ok(PaginaResponse.from(productoService.listar(page, size, filtro)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.desactivar(id));
    }
}
