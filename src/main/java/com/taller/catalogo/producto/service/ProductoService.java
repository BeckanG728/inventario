package com.taller.catalogo.producto.service;

import com.taller.catalogo.producto.dto.request.CreateProductoRequest;
import com.taller.catalogo.producto.dto.request.UpdateProductoRequest;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import org.springframework.data.domain.Page;

public interface ProductoService {

    ProductoResponse crear(CreateProductoRequest request);

    Page<ProductoResponse> listar(int page, int size, String nombre, Long categoriaId);

    ProductoResponse actualizar(Long id, UpdateProductoRequest request);

    ProductoResponse desactivar(Long id);
}
