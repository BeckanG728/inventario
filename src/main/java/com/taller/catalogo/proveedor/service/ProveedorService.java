package com.taller.catalogo.proveedor.service;

import com.taller.catalogo.proveedor.dto.request.ProveedorRequest;
import com.taller.catalogo.proveedor.dto.response.ProveedorResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProveedorService {

    ProveedorResponse crear(ProveedorRequest request);
    Page<ProveedorResponse> listar(int page, int size, String razonSocial, String ruc, Boolean activo);
    List<ProveedorResponse> listarActivos();
}