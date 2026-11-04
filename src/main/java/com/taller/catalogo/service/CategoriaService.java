package com.taller.catalogo.service;

import com.taller.catalogo.dto.request.CategoriaRequest;
import com.taller.catalogo.dto.response.CategoriaResponse;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);
}