package com.taller.auditoria.service;

import com.taller.auditoria.dto.response.AuditoriaResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface AuditoriaService {

    Page<AuditoriaResponse> listar(int page, int size, LocalDate desde, LocalDate hasta,
                                   String actor, String tipo, String resultado);
}
