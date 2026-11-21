package com.taller.seguridad.api.impl;

import com.taller.seguridad.api.UsuarioConsulta;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UsuarioConsultaImpl implements UsuarioConsulta {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> consultarUsuariosPorIds(List<Long> ids) {
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        usuario -> usuario.getId(),
                        usuario -> usuario.getUsername(),
                        (primero, segundo) -> primero));
    }
}
