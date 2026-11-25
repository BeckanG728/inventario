package com.taller.catalogo.unidad.service;

import com.taller.catalogo.unidad.dto.response.UnidadResponse;

import java.util.List;

public interface UnidadService {

    List<UnidadResponse> listarActivas();

    List<UnidadResponse> listarPorCategoria(Long categoriaId);
}
