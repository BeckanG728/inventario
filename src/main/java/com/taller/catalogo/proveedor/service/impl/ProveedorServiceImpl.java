package com.taller.catalogo.proveedor.service.impl;

import com.taller.catalogo.proveedor.dto.request.ProveedorRequest;
import com.taller.catalogo.proveedor.dto.response.ProveedorResponse;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import com.taller.catalogo.proveedor.service.ProveedorService;
import com.taller.catalogo.proveedor.specification.ProveedorSpecification;
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
public class ProveedorServiceImpl implements ProveedorService {

    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final ProveedorRepository repository;

    /** US-05 / RN109: el RUC debe ser único (409 si ya existe). */
    @Override
    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        String ruc = request.ruc() == null ? "" : request.ruc().trim();
        String razonSocial = request.razonSocial() == null ? "" : request.razonSocial().trim();
        if (ruc.isEmpty()) {
            throw new IllegalArgumentException("El RUC es obligatorio");
        }
        if (razonSocial.isEmpty()) {
            throw new IllegalArgumentException("La razón social es obligatoria");
        }
        if (repository.existsByRuc(ruc)) {
            throw new DuplicateResourceException("Ya existe un proveedor con el RUC " + ruc);
        }
        Proveedor saved = repository.save(new Proveedor(ruc, razonSocial,
                blankToNull(request.contacto()), blankToNull(request.telefono()),
                blankToNull(request.correo()), blankToNull(request.direccion())));
        return ProveedorResponse.from(saved);
    }

    /** Listado paginado: orden por razón social y luego id; filtros opcionales. */
    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponse> listar(int page, int size, String razonSocial, String ruc, Boolean activo) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO),
                Sort.by("razonSocial", "id"));

        Specification<Proveedor> filtros = Specification.unrestricted();
        if (razonSocial != null && !razonSocial.isBlank()) {
            filtros = filtros.and(ProveedorSpecification.razonSocialContiene(razonSocial.trim()));
        }
        if (ruc != null && !ruc.isBlank()) {
            filtros = filtros.and(ProveedorSpecification.rucContiene(ruc.trim()));
        }
        if (activo != null) {
            filtros = filtros.and(ProveedorSpecification.activoIgual(activo));
        }
        return repository.findAll(filtros, pageable).map(ProveedorResponse::from);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}