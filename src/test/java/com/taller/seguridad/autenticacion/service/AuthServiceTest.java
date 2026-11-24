package com.taller.seguridad.autenticacion.service;

import com.taller.seguridad.autenticacion.dto.request.LoginRequest;
import com.taller.seguridad.autenticacion.dto.request.RegisterRequest;
import com.taller.seguridad.autenticacion.dto.response.LoginResponse;
import com.taller.seguridad.autenticacion.dto.response.RegisterResponse;
import com.taller.seguridad.usuario.entity.Rol;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.mapper.UserMapper;
import com.taller.seguridad.usuario.repository.RolRepository;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import com.taller.seguridad.autenticacion.service.impl.AuthServiceImpl;
import com.taller.seguridad.exception.InvalidCredentialsException;
import com.taller.seguridad.exception.InvalidRoleException;
import com.taller.seguridad.exception.UserAlreadyExistsException;
import com.taller.seguridad.autenticacion.security.DetallesUsuario;
import com.taller.seguridad.autenticacion.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest request(String role) {
        return new RegisterRequest("carlos", "Pass123!", role);
    }

    private void stubSuccessfulRegister(String roleName) {
        when(rolRepository.findByNombre(roleName)).thenReturn(Optional.of(new Rol(roleName)));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toRegisterResponse(any(Usuario.class))).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            return new RegisterResponse(usuario.getId(), usuario.getUsername(),
                    usuario.getRol().getNombre(), usuario.getCreatedAt());
        });
    }

    // ---------- RN107: asignación de roles en el registro ----------

    @Test
    void rn107_registerWithJefeTaller_assignsJefeTaller() {
        stubSuccessfulRegister("JEFE_TALLER");
        RegisterResponse response = authService.register(request("JEFE_TALLER"));
        assertEquals("JEFE_TALLER", response.role());
    }

    @Test
    void rn107_registerWithExplicitMecanico_assignsMecanico() {
        stubSuccessfulRegister("MECANICO");
        RegisterResponse response = authService.register(request("MECANICO"));
        assertEquals("MECANICO", response.role());
    }

    @Test
    void rn107_registerWithAdmin_isRejectedAndNothingIsSaved() {
        assertThrows(InvalidRoleException.class, () -> authService.register(request("ADMIN")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rn107_registerWithUnknownRole_isRejected() {
        assertThrows(InvalidRoleException.class, () -> authService.register(request("SUPERUSER")));
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- RN109: unicidad (username) ----------

    @Test
    void rn109_duplicateUsername_throwsUserAlreadyExists() {
        when(usuarioRepository.existsByUsername("carlos")).thenReturn(true);
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request("MECANICO")));
        assertEquals("Username already in use", ex.getMessage());
    }

    @Test
    void rn109_newUsername_savesUser() {
        stubSuccessfulRegister("MECANICO");
        RegisterResponse response = authService.register(request("MECANICO"));
        assertEquals("carlos", response.username());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // ---------- Login ----------

    @Test
    void login_validCredentials_returnsToken() {
        DetallesUsuario detalles = new DetallesUsuario(1L, "carlos", "hashed", true, "MECANICO", 0L);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                detalles, null, detalles.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(1L, "MECANICO", "carlos", 0L)).thenReturn("jwt-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);
        when(userMapper.toLoginResponse(any(DetallesUsuario.class), eq("jwt-token"), eq(3600L)))
                .thenReturn(new LoginResponse("jwt-token", "Bearer", 3600L, 1L, "MECANICO"));

        LoginResponse response = authService.login(new LoginRequest("carlos", "Pass123!"));

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals(1L, response.userId());
        assertEquals("MECANICO", response.role());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_badCredentials_throwsInvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("nadie", "Pass123!")));
    }

    @Test
    void login_disabledUser_throwsInvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Disabled"));
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("carlos", "Pass123!")));
    }
}
