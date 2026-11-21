package com.taller.catalogo.categoria.service.impl;

import com.taller.catalogo.categoria.dto.request.CategoriaRequest;
import com.taller.catalogo.categoria.dto.response.CategoriaResponse;
import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.categoria.service.CategoriaService;
import com.taller.catalogo.categoria.specification.CategoriaSpecification;
import com.taller.compartido.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private static final int TAMANO_PAGINA_MAXIMO = 100;

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

    /** Listado paginado: orden por nombre y luego id; filtros opcionales por nombre (parcial) y estado. */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(int page, int size, String nombre, Boolean activo) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO),
                Sort.by("nombre", "id"));

        Specification<Categoria> filtros = Specification.unrestricted();
        if (nombre != null && !nombre.isBlank()) {
            filtros = filtros.and(CategoriaSpecification.nombreContiene(nombre.trim()));
        }
        if (activo != null) {
            filtros = filtros.and(CategoriaSpecification.activoIgual(activo));
        }
        return repository.findAll(filtros, pageable).map(CategoriaResponse::from);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}