package com.taller.controlstock.stockminimo.controller;

import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.controlstock.stockminimo.dto.request.StockMinimoRequest;
import com.taller.controlstock.stockminimo.service.StockMinimoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockMinimoController {

    private final StockMinimoService stockMinimoService;

    @PutMapping("/producto/{id}/minimo")
    public ResponseEntity<ProductoResponse> actualizarStockMinimo(
            @PathVariable Long id,
            @Valid @RequestBody StockMinimoRequest request) {
        return ResponseEntity.ok(stockMinimoService.actualizarStockMinimo(id, request.nuevoStockMinimo()));
    }
}
