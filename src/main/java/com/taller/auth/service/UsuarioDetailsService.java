package com.taller.auth.service;

import com.taller.auth.entity.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UsuarioDetailsService extends UserDetailsService {

    @Override
    Usuario loadUserByUsername(String username) throws UsernameNotFoundException;

    Usuario loadUserByUserID(Long userId) throws UsernameNotFoundException;
}
