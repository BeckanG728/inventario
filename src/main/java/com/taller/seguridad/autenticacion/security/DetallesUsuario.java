package com.taller.seguridad.autenticacion.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adaptador interno de {@code UserDetails} para {@code DaoAuthenticationProvider}.
 *
 * <p>Es el principal del proceso de login, no el expuesto mediante {@code @AuthenticationPrincipal}.
 * No contiene entidades JPA; el filtro JWT construye a partir de él un {@code ActorAutenticado}.</p>
 */
public record DetallesUsuario(
        Long id,
        String username,
        String passwordHash,
        boolean activo,
        String role
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
