package com.taller.catalogo.service;

import com.taller.catalogo.dto.request.ProveedorRequest;
import com.taller.catalogo.dto.response.ProveedorResponse;

public interface ProveedorService {

    ProveedorResponse crear(ProveedorRequest request);
}