package com.taller.catalogo.categoria.service.impl;

import com.taller.catalogo.categoria.dto.request.CategoriaRequest;
import com.taller.catalogo.categoria.dto.response.CategoriaResponse;
import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.categoria.service.CategoriaService;
import com.taller.catalogo.categoria.specification.CategoriaSpecification;
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.catalogo.unidad.repository.UnidadRepository;
import com.taller.compartido.exception.DuplicateResourceException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
    private final UnidadRepository unidadRepository;

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
        List<Long> unidadIds = request.unidadIds();
        if (unidadIds == null || unidadIds.isEmpty()) {
            // V10 siembra las relaciones de las categorías base. Para una categoría nueva,
            // UND es el mínimo seguro: no permite combinar unidades incoherentes.
            Unidad und = unidadRepository.findByCodigoIgnoreCase("UND")
                    .orElseThrow(() -> new IllegalStateException("FATAL: la unidad UND no existe, revisar V9"));
            unidadIds = List.of(und.getId());
        }
        Categoria categoria = new Categoria(nombre, blankToNull(request.descripcion()));
        categoria.reemplazarUnidades(resolverUnidades(unidadIds));
        Categoria saved = repository.save(categoria);
        return CategoriaResponse.from(saved);
    }

    private Set<Unidad> resolverUnidades(List<Long> unidadIds) {
        if (unidadIds == null || unidadIds.isEmpty()) {
            throw new IllegalArgumentException("La categoría debe tener al menos una unidad válida");
        }
        Set<Unidad> unidades = new LinkedHashSet<>();
        for (Long unidadId : unidadIds) {
            Unidad unidad = unidadRepository.findById(unidadId)
                    .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada: " + unidadId));
            if (!unidad.isActivo()) {
                throw new IllegalArgumentException("Unidad inactiva: " + unidad.getCodigo());
            }
            unidades.add(unidad);
        }
        return unidades;
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

    /** Listado ligero para combobox: solo categorías activas, sin paginar. */
    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivos() {
        return repository.findAll(CategoriaSpecification.activoIgual(true), Sort.by("nombre", "id"))
                .stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}