package com.taller.catalogo.service.impl;

import com.taller.catalogo.dto.request.ProveedorRequest;
import com.taller.catalogo.dto.response.ProveedorResponse;
import com.taller.catalogo.entity.Proveedor;
import com.taller.catalogo.repository.ProveedorRepository;
import com.taller.catalogo.service.ProveedorService;
import com.taller.common.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

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

    @Override
    @Transactional(readOnly = true)
    public Proveedor obtenerActivo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
        Proveedor proveedor = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        if (!proveedor.isActivo()) {
            throw new IllegalArgumentException("Proveedor inactivo");
        }
        return proveedor;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}