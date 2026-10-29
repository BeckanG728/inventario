package com.taller.catalogo.service;

import com.taller.catalogo.dto.request.ProveedorRequest;
import com.taller.catalogo.dto.response.ProveedorResponse;
import com.taller.catalogo.entity.Proveedor;

public interface ProveedorService {

    ProveedorResponse crear(ProveedorRequest request);

    /** Contrato para dev2 (RN105) y para las entradas (RN202): existe y está activo; si no, 400. */
    Proveedor obtenerActivo(Long id);
}