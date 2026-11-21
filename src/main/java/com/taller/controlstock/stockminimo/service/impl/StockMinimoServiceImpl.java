package com.taller.controlstock.stockminimo.service.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.controlstock.stockminimo.service.StockMinimoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockMinimoServiceImpl implements StockMinimoService {

    private final ProductoStock productoStock;

    @Override
    @Transactional
    public ProductoResponse actualizarStockMinimo(Long id, Integer nuevoStockMinimo) {
        if (nuevoStockMinimo == null || nuevoStockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo debe ser un número entero mayor o igual a 0");
        }
        return productoStock.fijarStockMinimo(id, nuevoStockMinimo);
    }
}
