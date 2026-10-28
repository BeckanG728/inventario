package com.taller.auth.service.impl;

import com.taller.auth.dto.request.LoginRequest;
import com.taller.auth.dto.request.RegisterRequest;
import com.taller.auth.dto.response.LoginResponse;
import com.taller.auth.dto.response.RegisterResponse;
import com.taller.auth.entity.Rol;
import com.taller.auth.entity.Usuario;
import com.taller.auth.enums.UserRole;
import com.taller.auth.exception.InvalidCredentialsException;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UserAlreadyExistsException;
import com.taller.auth.mapper.UserMapper;
import com.taller.auth.repository.RolRepository;
import com.taller.auth.repository.UsuarioRepository;
import com.taller.auth.security.JwtUtil;
import com.taller.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        UserRole role = resolveRole(request.role());

        String username = request.username().trim();

        if (usuarioRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        Rol rol = rolRepository.findByNombre(role.name())
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + role.name()));

        Usuario saved = usuarioRepository.save(
                new Usuario(username, passwordEncoder.encode(request.password()), rol));

        return userMapper.toRegisterResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = request.username().trim();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();
        String role = usuario.getRol().getNombre();
        String token = jwtUtil.generateToken(usuario.getId(), role, usuario.getUsername());

        return userMapper.toLoginResponse(usuario, token, jwtUtil.getExpirationSeconds());
    }

    /**
     * ADMIN nunca se asigna por registro (usuario seed).
     */
    private UserRole resolveRole(String requestedRole) {
        UserRole role;
        try {
            role = UserRole.valueOf(requestedRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException("Rol inválido: " + requestedRole);
        }
        if (!role.validRole()) {
            throw new InvalidRoleException("El rol ADMIN no puede asignarse mediante el registro");
        }
        return role;
    }
}
