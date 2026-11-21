package com.taller.seguridad.usuario.service;

import com.taller.seguridad.usuario.dto.request.ActualizarUsuarioRequest;
import com.taller.seguridad.usuario.dto.request.ResetPasswordRequest;
import com.taller.seguridad.usuario.dto.response.UsuarioResponse;
import org.springframework.data.domain.Page;

public interface UsuarioService {

    Page<UsuarioResponse> listar(int page, int size, String username, String role, Boolean activo);

    UsuarioResponse obtener(Long id);

    UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request);

    UsuarioResponse resetPassword(Long id, ResetPasswordRequest request);
}
