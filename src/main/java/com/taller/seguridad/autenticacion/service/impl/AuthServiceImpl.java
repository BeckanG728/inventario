package com.taller.seguridad.autenticacion.service.impl;

import com.taller.seguridad.autenticacion.dto.request.LoginRequest;
import com.taller.seguridad.autenticacion.dto.request.RegisterRequest;
import com.taller.seguridad.autenticacion.dto.response.LoginResponse;
import com.taller.seguridad.autenticacion.dto.response.RegisterResponse;
import com.taller.seguridad.usuario.entity.Rol;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.enums.UserRole;
import com.taller.seguridad.usuario.mapper.UserMapper;
import com.taller.seguridad.usuario.repository.RolRepository;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import com.taller.seguridad.autenticacion.service.AuthService;
import com.taller.seguridad.exception.InvalidCredentialsException;
import com.taller.seguridad.exception.InvalidRoleException;
import com.taller.seguridad.exception.UserAlreadyExistsException;
import com.taller.seguridad.autenticacion.security.DetallesUsuario;
import com.taller.seguridad.autenticacion.security.JwtUtil;
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

        DetallesUsuario detalles = (DetallesUsuario) authentication.getPrincipal();
        String token = jwtUtil.generateToken(detalles.id(), detalles.role(), detalles.username(), detalles.versionSesion());

        return userMapper.toLoginResponse(detalles, token, jwtUtil.getExpirationSeconds());
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
