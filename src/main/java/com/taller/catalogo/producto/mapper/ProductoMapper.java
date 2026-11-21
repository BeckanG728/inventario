package com.taller.catalogo.producto.mapper;

import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "stockCritico",
            expression = "java(producto.getStockActual() <= producto.getStockMinimo())")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNombre", source = "categoria.nombre")
    @Mapping(target = "proveedorId", source = "proveedor.id")
    @Mapping(target = "proveedorRazonSocial", source = "proveedor.razonSocial")
    ProductoResponse toResponse(Producto producto);
}
