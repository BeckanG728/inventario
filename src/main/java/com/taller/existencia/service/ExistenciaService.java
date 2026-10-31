package com.taller.existencia.service;

import com.taller.existencia.dto.response.ExistenciaResponse;
import java.util.List;

public interface ExistenciaService {

    List<ExistenciaResponse> consultar(List<Long> ids);
}