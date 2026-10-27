package com.taller.sec.service;

import com.taller.sec.dto.LoginRequest;
import com.taller.sec.dto.LoginResponse;
import com.taller.sec.dto.RegisterRequest;
import com.taller.sec.dto.RegisterResponse;
import com.taller.sec.entity.Rol;
import com.taller.sec.entity.UserRole;
import com.taller.sec.entity.Usuario;
import com.taller.sec.exception.InvalidCredentialsException;
import com.taller.sec.exception.UserAlreadyExistsException;
import com.taller.sec.repository.RolRepository;
import com.taller.sec.repository.UsuarioRepository;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validatePasswordStrength(request.password());
        UserRole role = resolveRole(request.role());

        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (usuarioRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        if (usuarioRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        Rol rol = rolRepository.findByNombre(role.name())
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + role.name()));

        Usuario saved = usuarioRepository.save(
                new Usuario(username, email, passwordEncoder.encode(request.password()), rol));

        return new RegisterResponse(saved.getId(), saved.getUsername(), saved.getEmail(),
                saved.getRol().getNombre(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

        if (!usuario.isActivo() || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        return new LoginResponse(
                jwtService.generateToken(usuario),
                "Bearer",
                jwtService.getExpirationSeconds(),
                usuario.getId(),
                usuario.getRol().getNombre());
    }

    /** Sin rol -> MECANICO. ADMIN nunca se asigna por registro (usuario seed). */
    private UserRole resolveRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return UserRole.MECANICO;
        }
        UserRole role;
        try {
            role = UserRole.valueOf(requestedRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Rol inválido: " + requestedRole);
        }
        if (role == UserRole.ADMIN) {
            throw new IllegalArgumentException("El rol ADMIN no puede asignarse mediante el registro");
        }
        return role;
    }

    private void validatePasswordStrength(String password) {
        if (password == null
                || password.length() < 8
                || !UPPERCASE.matcher(password).find()
                || !DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener mínimo 8 caracteres, al menos 1 mayúscula y 1 número");
        }
    }
}