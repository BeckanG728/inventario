package com.taller.seguridad.usuario.service.impl;

import com.taller.seguridad.usuario.entity.Rol;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import com.taller.seguridad.usuario.service.impl.UsuarioDetailsServiceImpl;
import com.taller.seguridad.autenticacion.security.DetallesUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsServiceImpl service;

    @Test
    void loadUserByUsername_findsByUsername() {
        Usuario usuario = usuario(4L);
        when(usuarioRepository.findByUsername("carlos")).thenReturn(Optional.of(usuario));

        DetallesUsuario details = service.loadUserByUsername("carlos");

        assertEquals("carlos", details.getUsername());
        assertEquals(4L, details.id());
    }

    @Test
    void loadUserByUserID_findsById() {
        Usuario usuario = usuario(4L);
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario));

        DetallesUsuario details = service.loadUserByUserID(4L);

        assertEquals(4L, details.id());
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario("carlos", "hashed", new Rol("MECANICO"));
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }
}
