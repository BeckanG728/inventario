package com.taller.catalogo.categoria.service;

import com.taller.catalogo.categoria.dto.request.CategoriaRequest;
import com.taller.catalogo.categoria.dto.response.CategoriaResponse;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);
}