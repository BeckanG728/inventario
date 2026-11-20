package com.taller.auditoria.service.impl;

import com.taller.auditoria.dto.response.AuditoriaResponse;
import com.taller.auditoria.mapper.AuditoriaMapper;
import com.taller.auditoria.repository.AuditoriaOperacionRepository;
import com.taller.auditoria.service.AuditoriaService;
import com.taller.auditoria.specification.AuditoriaSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final AuditoriaOperacionRepository repository;
    private final AuditoriaMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaResponse> listar(int page, int size, LocalDate desde, LocalDate hasta,
                                          String actor, String tipo, String resultado) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO),
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id")));

        return repository.findAll(
                        AuditoriaSpecification.filtros(desde, hasta, actor, tipo, resultado),
                        pageable)
                .map(mapper::toResponse);
    }
}
