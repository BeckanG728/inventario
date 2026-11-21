package com.taller.catalogo.api.impl;

import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.mapper.ProductoMapper;
import com.taller.catalogo.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductoStockMinimoImpl implements ProductoStockMinimo {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> consultarBajoStock() {
        return productoRepository.findAllByActivoTrue().stream()
                .filter(p -> p.getStockActual() <= p.getStockMinimo())
                .map(productoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> consultarActivos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> consultarPorIds(List<Long> ids) {
        return productoRepository.findAllById(ids).stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarActivos() {
        return productoRepository.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayProductos() {
        return productoRepository.count() > 0;
    }
}
