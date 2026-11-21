package com.taller.catalogo.api;

import com.taller.catalogo.producto.dto.response.ProductoResponse;

import java.util.List;

/**
 * Consultas públicas de catálogo sobre productos y su stock.
 *
 * <p>Se reutiliza {@link ProductoResponse} porque ya representa exactamente la información
 * pública necesaria (identificador, código, nombre, stock, mínimo, razón social y costo).</p>
 */
public interface ProductoStockMinimo {

    /** Productos activos con stock actual menor o igual al mínimo. */
    List<ProductoResponse> consultarBajoStock();

    /** Productos activos, ordenados por nombre (usado por valorización y alertas). */
    List<ProductoResponse> consultarActivos();

    /** Productos encontrados por identificador, activos o no. No lanza si faltan ids. */
    List<ProductoResponse> consultarPorIds(List<Long> ids);

    long contarActivos();

    boolean hayProductos();
}
