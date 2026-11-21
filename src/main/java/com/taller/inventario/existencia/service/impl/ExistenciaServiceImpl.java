package com.taller.inventario.existencia.service.impl;

import com.taller.catalogo.api.ProductoStockMinimo;
import com.taller.inventario.existencia.dto.response.ExistenciaResponse;
import com.taller.inventario.existencia.service.ExistenciaService;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.exception.ProductoNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExistenciaServiceImpl implements ExistenciaService {

    private final ProductoStockMinimo productoStockMinimo;

    @Override
    @Transactional(readOnly = true)
    public List<ExistenciaResponse> consultar(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("El parámetro ids es obligatorio");
        }

        List<Long> unicos = ids.stream().distinct().toList();
        Map<Long, ProductoResponse> porId = productoStockMinimo.consultarPorIds(unicos).stream()
                .collect(Collectors.toMap(ProductoResponse::id, Function.identity()));

        List<ExistenciaResponse> respuesta = new ArrayList<>();
        for (Long id : unicos) {
            ProductoResponse producto = porId.get(id);
            if (producto == null) {
                throw new ProductoNotFoundException(id);              // 404
            }
            if (!producto.activo()) {
                throw new IllegalArgumentException("El producto " + id + " está inactivo"); // 400
            }
            respuesta.add(ExistenciaResponse.from(producto));
        }
        return respuesta;
    }
}
