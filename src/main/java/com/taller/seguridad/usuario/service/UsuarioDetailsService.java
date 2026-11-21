package com.taller.seguridad.usuario.service;

import com.taller.seguridad.autenticacion.security.DetallesUsuario;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UsuarioDetailsService extends UserDetailsService {

    @Override
    DetallesUsuario loadUserByUsername(String username) throws UsernameNotFoundException;

    DetallesUsuario loadUserByUserID(Long userId) throws UsernameNotFoundException;
}
