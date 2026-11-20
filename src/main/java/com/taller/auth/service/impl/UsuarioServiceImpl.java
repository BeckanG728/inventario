package com.taller.auth.service.impl;

import com.taller.auth.dto.request.ActualizarUsuarioRequest;
import com.taller.auth.dto.request.ResetPasswordRequest;
import com.taller.auth.dto.response.UsuarioResponse;
import com.taller.auth.entity.Rol;
import com.taller.auth.entity.Usuario;
import com.taller.auth.enums.UserRole;
import com.taller.auth.exception.InvalidRoleException;
import com.taller.auth.exception.UltimoAdminActivoException;
import com.taller.auth.exception.UsuarioNotFoundException;
import com.taller.auth.mapper.UserMapper;
import com.taller.auth.repository.RolRepository;
import com.taller.auth.repository.UsuarioRepository;
import com.taller.auth.service.UsuarioService;
import com.taller.auth.specification.UsuarioSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(int page, int size, String username, String role, Boolean activo) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO),
                Sort.by("username", "id"));
        String filtroUsername = (username == null || username.isBlank()) ? null : username.trim();
        String filtroRol = resolverFiltroRol(role);

        Specification<Usuario> filtros = Specification.unrestricted();
        if (filtroUsername != null) {
            filtros = filtros.and(UsuarioSpecification.usernameContiene(filtroUsername));
        }
        if (filtroRol != null) {
            filtros = filtros.and(UsuarioSpecification.rolIgual(filtroRol));
        }
        if (activo != null) {
            filtros = filtros.and(UsuarioSpecification.activoIgual(activo));
        }

        return usuarioRepository.findAll(filtros, pageable).map(userMapper::toUsuarioResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return userMapper.toUsuarioResponse(obtenerUsuario(id));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = obtenerUsuario(id);
        UserRole nuevoRol = resolverRol(request.role());
        boolean nuevoActivo = request.activo();

        if (esUltimoAdminActivo(usuario, nuevoRol, nuevoActivo)) {
            throw new UltimoAdminActivoException();
        }

        Rol rol = rolRepository.findByNombre(nuevoRol.name())
                .orElseThrow(() -> new IllegalStateException("Rol no configurado: " + nuevoRol.name()));
        usuario.setRol(rol);
        usuario.setActivo(nuevoActivo);

        return userMapper.toUsuarioResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse resetPassword(Long id, ResetPasswordRequest request) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        return userMapper.toUsuarioResponse(usuario);
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    private boolean esUltimoAdminActivo(Usuario usuario, UserRole nuevoRol, boolean nuevoActivo) {
        boolean esAdminActual = UserRole.ADMIN.name().equals(usuario.getRol().getNombre());
        boolean dejaDeSerAdminActivo = esAdminActual && (!nuevoActivo || nuevoRol != UserRole.ADMIN);
        if (!dejaDeSerAdminActivo) {
            return false;
        }
        return usuarioRepository.countByActivoTrueAndRolNombre(UserRole.ADMIN.name()) <= 1;
    }

    private String resolverFiltroRol(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        return resolverRol(role).name();
    }

    private UserRole resolverRol(String role) {
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException("Rol inválido: " + role);
        }
    }
}
