package com.taller.seguridad.usuario.service.impl;

import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import com.taller.seguridad.usuario.service.UsuarioDetailsService;
import com.taller.seguridad.autenticacion.security.DetallesUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsServiceImpl implements UsuarioDetailsService {

    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

    private final UsuarioRepository usuarioRepository;

    @Override
    public DetallesUsuario loadUserByUsername(String username) throws UsernameNotFoundException {
        return aDetalles(usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USUARIO_NO_ENCONTRADO)));
    }

    @Override
    public DetallesUsuario loadUserByUserID(Long userId) throws UsernameNotFoundException {
        return aDetalles(usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(USUARIO_NO_ENCONTRADO)));
    }

    private DetallesUsuario aDetalles(Usuario usuario) {
        return new DetallesUsuario(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isActivo(),
                usuario.getRol().getNombre(),
                usuario.getVersionSesion());
    }
}
