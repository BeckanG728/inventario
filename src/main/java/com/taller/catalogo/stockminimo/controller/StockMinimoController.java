package com.taller.catalogo.stockminimo.controller;

import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.stockminimo.dto.request.StockMinimoRequest;
import com.taller.catalogo.stockminimo.service.StockMinimoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('JEFE_TALLER')")
public class StockMinimoController {

    private final StockMinimoService stockMinimoService;

    @PutMapping("/{id}/stock-minimo")
    public ResponseEntity<ProductoResponse> actualizarStockMinimo(
            @PathVariable Long id,
            @Valid @RequestBody StockMinimoRequest request) {
        return ResponseEntity.ok(stockMinimoService.actualizarStockMinimo(id, request.nuevoStockMinimo()));
    }
}
