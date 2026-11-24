package com.taller.seguridad.usuario.service.impl;

import com.taller.seguridad.usuario.dto.request.ActualizarUsuarioRequest;
import com.taller.seguridad.usuario.dto.request.ResetPasswordRequest;
import com.taller.seguridad.usuario.dto.response.UsuarioResponse;
import com.taller.seguridad.usuario.entity.Rol;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.mapper.UserMapper;
import com.taller.seguridad.usuario.repository.RolRepository;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import com.taller.seguridad.usuario.service.impl.UsuarioServiceImpl;
import com.taller.seguridad.exception.InvalidRoleException;
import com.taller.seguridad.exception.UltimoAdminActivoException;
import com.taller.seguridad.exception.UsuarioNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UsuarioServiceImpl service;

    @Test
    void actualizar_ultimoAdminDesactivar_lanzaUltimoAdminActivoException() {
        Usuario admin = usuario(1L, "ADMIN");
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByActivoTrueAndRolNombre("ADMIN")).thenReturn(1L);

        assertThrows(UltimoAdminActivoException.class,
                () -> service.actualizar(1L, new ActualizarUsuarioRequest("ADMIN", false)));

        verify(rolRepository, never()).findByNombre(any());
    }

    @Test
    void actualizar_ultimoAdminCambiarRol_lanzaUltimoAdminActivoException() {
        Usuario admin = usuario(1L, "ADMIN");
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByActivoTrueAndRolNombre("ADMIN")).thenReturn(1L);

        assertThrows(UltimoAdminActivoException.class,
                () -> service.actualizar(1L, new ActualizarUsuarioRequest("MECANICO", true)));
    }

    @Test
    void actualizar_conOtroAdminActivo_permiteDesactivar() {
        Usuario admin = usuario(1L, "ADMIN");
        Rol rolAdmin = new Rol("ADMIN");
        when(usuarioRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByActivoTrueAndRolNombre("ADMIN")).thenReturn(2L);
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin));
        when(userMapper.toUsuarioResponse(admin)).thenReturn(respuesta(admin, "ADMIN"));

        service.actualizar(1L, new ActualizarUsuarioRequest("ADMIN", false));

        assertFalse(admin.isActivo());
    }

    @Test
    void obtener_usuarioInexistente_lanzaUsuarioNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () -> service.obtener(99L));
    }

    @Test
    void resetPassword_codificaLaNuevaContraseña() {
        Usuario usuario = usuario(2L, "MECANICO");
        when(usuarioRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("Pass123!")).thenReturn("hash-bcrypt");
        when(userMapper.toUsuarioResponse(usuario)).thenReturn(respuesta(usuario, "MECANICO"));

        service.resetPassword(2L, new ResetPasswordRequest("Pass123!"));

        assertEquals("hash-bcrypt", usuario.getPassword());
        verify(passwordEncoder).encode("Pass123!");
    }

    @Test
    void listar_filtroRolInvalido_lanzaInvalidRoleException() {
        assertThrows(InvalidRoleException.class,
                () -> service.listar(0, 10, null, "ROL_DESCONOCIDO", null));
    }

    private Usuario usuario(Long id, String rol) {
        Usuario usuario = new Usuario("usuario" + id, "hash", new Rol(rol));
        usuario.setId(id);
        return usuario;
    }

    private UsuarioResponse respuesta(Usuario usuario, String rol) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), rol,
                usuario.isActivo(), usuario.getCreatedAt());
    }
}
