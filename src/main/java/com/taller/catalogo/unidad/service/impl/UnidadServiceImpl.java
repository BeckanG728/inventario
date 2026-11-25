package com.taller.catalogo.unidad.service.impl;

import com.taller.catalogo.unidad.dto.response.UnidadResponse;
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.catalogo.unidad.repository.UnidadRepository;
import com.taller.catalogo.unidad.service.UnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadServiceImpl implements UnidadService {

    private final UnidadRepository unidadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UnidadResponse> listarActivas() {
        return unidadRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(UnidadResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadResponse> listarPorCategoria(Long categoriaId) {
        if (categoriaId == null || categoriaId <= 0) {
            throw new IllegalArgumentException("El id de categoría debe ser mayor que cero");
        }
        return unidadRepository.findActivasPorCategoria(categoriaId).stream()
                .map(UnidadResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public UnidadResponse desactivar(Long id) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada"));
        if (unidadRepository.esUnidadEnUso(id)) {
            throw new IllegalArgumentException("No se puede desactivar una unidad usada por algún producto");
        }
        unidad.setActivo(false);
        return UnidadResponse.from(unidad);
    }
}
