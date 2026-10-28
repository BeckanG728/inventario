package com.taller.auth.service;

import com.taller.auth.dto.request.LoginRequest;
import com.taller.auth.dto.request.RegisterRequest;
import com.taller.auth.dto.response.LoginResponse;
import com.taller.auth.dto.response.RegisterResponse;
import com.taller.auth.entity.Rol;
import com.taller.auth.entity.Usuario;
import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.mapper.UserMapper;
import com.taller.auth.repository.RolRepository;
import com.taller.auth.repository.UsuarioRepository;
import com.taller.auth.security.JwtUtil;
import com.taller.auth.service.impl.AuthServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

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
        return new RegisterRequest("carlos", "carlos@test.com", "Pass123!", role);
    }

    private void stubSuccessfulRegister(String roleName) {
        when(rolRepository.findByNombre(roleName)).thenReturn(Optional.of(new Rol(roleName)));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toRegisterResponse(any(Usuario.class))).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            return new RegisterResponse(usuario.getId(), usuario.getUsername(), usuario.getEmail(),
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

    // ---------- RN109: unicidad (email / username) ----------

    @Test
    void rn109_duplicateEmail_throwsUserAlreadyExists() {
        when(usuarioRepository.existsByEmail("carlos@test.com")).thenReturn(true);
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request("MECANICO")));
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    void rn109_duplicateEmail_doesNotSave() {
        when(usuarioRepository.existsByEmail("carlos@test.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request("MECANICO")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rn109_duplicateUsername_throwsUserAlreadyExists() {
        when(usuarioRepository.existsByUsername("carlos")).thenReturn(true);
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request("MECANICO")));
        assertEquals("Username already in use", ex.getMessage());
    }

    @Test
    void rn109_emailIsNormalizedBeforeCheckingUniqueness() {
        when(usuarioRepository.existsByEmail("carlos@test.com")).thenReturn(true);
        RegisterRequest upperCaseEmail = new RegisterRequest("carlos", "Carlos@Test.com", "Pass123!", "MECANICO");
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(upperCaseEmail));
    }

    @Test
    void rn109_newEmailAndUsername_savesUser() {
        stubSuccessfulRegister("MECANICO");
        RegisterResponse response = authService.register(request("MECANICO"));
        assertEquals("carlos", response.username());
        assertEquals("carlos@test.com", response.email());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // ---------- Login ----------

    @Test
    void login_validCredentials_returnsToken() {
        Usuario usuario = new Usuario("carlos", "carlos@test.com", "hashed", new Rol("MECANICO"));
        ReflectionTestUtils.setField(usuario, "id", 1L);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, usuario.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(1L, "MECANICO", "carlos")).thenReturn("jwt-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);
        when(userMapper.toLoginResponse(any(Usuario.class), eq("jwt-token"), eq(3600L)))
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
