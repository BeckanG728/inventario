package com.taller.sec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taller.sec.dto.LoginRequest;
import com.taller.sec.dto.LoginResponse;
import com.taller.sec.dto.RegisterRequest;
import com.taller.sec.dto.RegisterResponse;
import com.taller.sec.entity.Rol;
import com.taller.sec.entity.Usuario;
import com.taller.sec.exception.InvalidCredentialsException;
import com.taller.sec.exception.UserAlreadyExistsException;
import com.taller.sec.repository.RolRepository;
import com.taller.sec.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    private RegisterRequest request(String role) {
        return new RegisterRequest("carlos", "carlos@test.com", "Pass123!", role);
    }

    private void stubSuccessfulRegister(String roleName) {
        when(rolRepository.findByNombre(roleName)).thenReturn(Optional.of(new Rol(roleName)));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- RN107: asignación de roles en el registro ----------

    @Test
    void rn107_registerWithoutRole_assignsMecanico() {
        stubSuccessfulRegister("MECANICO");
        RegisterResponse response = authService.register(request(null));
        assertEquals("MECANICO", response.role());
    }

    @Test
    void rn107_registerWithBlankRole_assignsMecanico() {
        stubSuccessfulRegister("MECANICO");
        RegisterResponse response = authService.register(request("  "));
        assertEquals("MECANICO", response.role());
    }

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
        assertThrows(IllegalArgumentException.class, () -> authService.register(request("ADMIN")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rn107_registerWithUnknownRole_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> authService.register(request("SUPERUSER")));
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- RN109: unicidad (email / username) ----------

    @Test
    void rn109_duplicateEmail_throwsUserAlreadyExists() {
        when(usuarioRepository.existsByEmail("carlos@test.com")).thenReturn(true);
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request(null)));
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    void rn109_duplicateEmail_doesNotSave() {
        when(usuarioRepository.existsByEmail("carlos@test.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request(null)));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rn109_duplicateUsername_throwsUserAlreadyExists() {
        when(usuarioRepository.existsByUsername("carlos")).thenReturn(true);
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request(null)));
        assertEquals("Username already in use", ex.getMessage());
    }

    @Test
    void rn109_emailIsNormalizedBeforeCheckingUniqueness() {
        when(usuarioRepository.existsByEmail("carlos@test.com")).thenReturn(true);
        RegisterRequest upperCaseEmail = new RegisterRequest("carlos", "Carlos@Test.com", "Pass123!", null);
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(upperCaseEmail));
    }

    @Test
    void rn109_newEmailAndUsername_savesUser() {
        stubSuccessfulRegister("MECANICO");
        RegisterResponse response = authService.register(request(null));
        assertEquals("carlos", response.username());
        assertEquals("carlos@test.com", response.email());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    // ---------- Contraseña ----------

    @ParameterizedTest
    @ValueSource(strings = {"Ab1", "abcdefg1", "Abcdefgh"})
    void register_weakPassword_isRejected(String weakPassword) {
        RegisterRequest weak = new RegisterRequest("carlos", "carlos@test.com", weakPassword, null);
        assertThrows(IllegalArgumentException.class, () -> authService.register(weak));
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- Login ----------

    @Test
    void login_validCredentials_returnsToken() {
        Usuario usuario = new Usuario("carlos", "carlos@test.com", "hashed", new Rol("MECANICO"));
        when(usuarioRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Pass123!", "hashed")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        LoginResponse response = authService.login(new LoginRequest("carlos@test.com", "Pass123!"));

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertEquals("MECANICO", response.role());
    }

    @Test
    void login_unknownEmail_throwsInvalidCredentials() {
        when(usuarioRepository.findByEmail("nadie@test.com")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("nadie@test.com", "Pass123!")));
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        Usuario usuario = new Usuario("carlos", "carlos@test.com", "hashed", new Rol("MECANICO"));
        when(usuarioRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Incorrecta1", "hashed")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("carlos@test.com", "Incorrecta1")));
    }
}