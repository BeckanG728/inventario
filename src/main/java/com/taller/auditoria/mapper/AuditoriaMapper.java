package com.taller.auditoria.mapper;

import com.taller.auditoria.domain.AuditoriaOperacion;
import com.taller.auditoria.dto.response.AuditoriaResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditoriaMapper {

    AuditoriaResponse toResponse(AuditoriaOperacion operacion);
}
