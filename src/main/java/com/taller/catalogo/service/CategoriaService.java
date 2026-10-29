package com.taller.catalogo.service;

import com.taller.catalogo.dto.request.CategoriaRequest;
import com.taller.catalogo.dto.response.CategoriaResponse;
import com.taller.catalogo.entity.Categoria;

public interface CategoriaService {

    CategoriaResponse crear(CategoriaRequest request);

    /** Contrato para dev2 (RN102): existe y está activa; si no, 400 ("Categoría inactiva"). */
    Categoria obtenerActiva(Long id);
}