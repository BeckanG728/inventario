package com.taller.producto.service;

import com.taller.producto.dto.request.CreateProductoRequest;
import com.taller.producto.dto.request.ProductoFiltro;
import com.taller.producto.dto.request.UpdateProductoRequest;
import com.taller.producto.dto.response.ProductoResponse;
import org.springframework.data.domain.Page;

public interface ProductoService {

    ProductoResponse crear(CreateProductoRequest request);

    ProductoResponse actualizar(Long id, UpdateProductoRequest request);

    ProductoResponse desactivar(Long id);

    ProductoResponse actualizarStockMinimo(Long id, Integer nuevoStockMinimo);

    Page<ProductoResponse> listar(int page, int size, ProductoFiltro filtro);
}
