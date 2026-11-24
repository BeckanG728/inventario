package com.taller.monitoreo.service;

import com.taller.monitoreo.dto.request.ExistenciaConsultaFiltros;
import com.taller.monitoreo.dto.response.ExistenciaConsultaResponse;
import org.springframework.data.domain.Page;

public interface ExistenciaService {

    Page<ExistenciaConsultaResponse> listarActivos(ExistenciaConsultaFiltros filtros);
}
