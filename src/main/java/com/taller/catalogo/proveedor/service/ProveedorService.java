package com.taller.catalogo.proveedor.service;

import com.taller.catalogo.proveedor.dto.request.ProveedorRequest;
import com.taller.catalogo.proveedor.dto.response.ProveedorResponse;

public interface ProveedorService {

    ProveedorResponse crear(ProveedorRequest request);
}