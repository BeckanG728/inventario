package com.taller.catalogo.categoria.service;

import com.taller.catalogo.categoria.dto.request.CategoriaRequest;
import com.taller.catalogo.categoria.dto.response.CategoriaResponse;
import org.springframework.data.domain.Page;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);
    Page<CategoriaResponse> listar(int page, int size, String nombre, Boolean activo);
}