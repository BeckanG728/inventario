package com.taller.catalogo.stockminimo.service;

import com.taller.catalogo.producto.dto.response.ProductoResponse;

public interface StockMinimoService {

    ProductoResponse actualizarStockMinimo(Long id, Integer nuevoStockMinimo);
}
