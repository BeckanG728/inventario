package com.taller.auth.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.auth.repository.UsuarioRepository;
import com.taller.auth.service.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsServiceImpl implements UsuarioDetailsService {

    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";

    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(USUARIO_NO_ENCONTRADO));
    }

    @Override
    public Usuario loadUserByUserID(Long userId) throws UsernameNotFoundException {
        return usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(USUARIO_NO_ENCONTRADO));
    }
}
