package com.taller.inventario.existencia.service;

import com.taller.inventario.existencia.dto.response.ExistenciaResponse;
import java.util.List;

public interface ExistenciaService {

    List<ExistenciaResponse> consultar(List<Long> ids);
}