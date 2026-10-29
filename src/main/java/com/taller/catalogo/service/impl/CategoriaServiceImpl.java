package com.taller.catalogo.service.impl;

import com.taller.catalogo.dto.request.CategoriaRequest;
import com.taller.catalogo.dto.response.CategoriaResponse;
import com.taller.catalogo.entity.Categoria;
import com.taller.catalogo.repository.CategoriaRepository;
import com.taller.catalogo.service.CategoriaService;
import com.taller.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository repository;

    /** US-05 / RN102: nombre obligatorio y único. */
    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre() == null ? "" : request.nombre().trim();
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre " + nombre);
        }
        Categoria saved = repository.save(new Categoria(nombre, blankToNull(request.descripcion())));
        return CategoriaResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtenerActiva(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        Categoria categoria = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        if (!categoria.isActivo()) {
            throw new IllegalArgumentException("Categoría inactiva");
        }
        return categoria;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}