package com.taller.catalogo.api.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.entity.Producto;
import com.taller.catalogo.producto.exception.ProductoNotFoundException;
import com.taller.catalogo.producto.mapper.ProductoMapper;
import com.taller.catalogo.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProductoStockImpl implements ProductoStock {

    private static final String NO_DISPONIBLE = "Producto ID %d no encontrado o inactivo";

    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional
    public ResultadoStock descontarStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findByIdAndActivoTrueForUpdate(productoId)
                .orElseThrow(() -> new IllegalArgumentException(NO_DISPONIBLE.formatted(productoId)));
        int anterior = producto.getStockActual();
        producto.descontarStock(cantidad);
        return new ResultadoStock(productoMapper.toResponse(producto), anterior, producto.getStockActual());
    }

    @Override
    @Transactional
    public ResultadoStock fijarStock(Long productoId, int cantidad) {
        Producto producto = productoRepository.findByIdAndActivoTrueForUpdate(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        int anterior = producto.getStockActual();
        producto.setStockActual(cantidad);
        return new ResultadoStock(productoMapper.toResponse(producto), anterior, producto.getStockActual());
    }

    @Override
    @Transactional
    public ProductoResponse fijarStockMinimo(Long productoId, Integer stockMinimo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        producto.setStockMinimo(stockMinimo);
        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional
    public ResultadoEntrada aplicarEntrada(Long productoId, Long proveedorId, int cantidad,
                                          BigDecimal costoUnitario) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser mayor que cero");
        }
        if (costoUnitario == null || costoUnitario.signum() < 0) {
            throw new IllegalArgumentException("Costo unitario no puede ser negativo");
        }

        Producto producto = productoRepository.findByIdAndActivoTrueForUpdate(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        Proveedor proveedor = proveedorRepository.findByIdAndActivoTrue(proveedorId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado o inactivo"));

        int anterior = producto.getStockActual();
        int posterior;
        try {
            posterior = Math.addExact(anterior, cantidad);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("La cantidad excede el stock máximo permitido");
        }

        producto.setStockActual(posterior);
        producto.setCostoUltimaCompra(costoUnitario);

        return new ResultadoEntrada(
                productoMapper.toResponse(producto),
                anterior,
                posterior,
                "Entrada de proveedor " + proveedor.getRazonSocial());
    }
}
