package com.taller.catalogo.producto.dto.request;

/** Filtros opcionales del listado de productos. Un valor null significa "sin filtro". */
public record ProductoFiltro(
        String nombre,
        Long categoriaId,
        String codigo,
        Long proveedorId,
        Long unidadBaseId,
        Boolean activo,
        Boolean stockCritico
) {
}
