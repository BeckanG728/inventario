package com.taller.existencia.service.impl;

import com.taller.existencia.dto.response.ExistenciaResponse;
import com.taller.existencia.service.ExistenciaService;
import com.taller.producto.entity.Producto;
import com.taller.producto.exception.ProductoNotFoundException;
import com.taller.producto.repository.ProductoRepository;
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

    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ExistenciaResponse> consultar(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("El parámetro ids es obligatorio");
        }

        List<Long> unicos = ids.stream().distinct().toList();
        Map<Long, Producto> porId = productoRepository.findAllById(unicos).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        List<ExistenciaResponse> respuesta = new ArrayList<>();
        for (Long id : unicos) {
            Producto producto = porId.get(id);
            if (producto == null) {
                throw new ProductoNotFoundException(id);              // 404
            }
            if (!producto.isActivo()) {
                throw new IllegalArgumentException("El producto " + id + " está inactivo"); // 400
            }
            respuesta.add(ExistenciaResponse.from(producto));
        }
        return respuesta;
    }
}