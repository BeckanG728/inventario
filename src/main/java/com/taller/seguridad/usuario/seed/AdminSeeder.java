package com.taller.seguridad.usuario.seed;

import com.taller.seguridad.config.SeguridadProperties;
import com.taller.seguridad.usuario.entity.Rol;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.repository.RolRepository;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       SeguridadProperties appConfig) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = appConfig.getAdminUsername() == null
                ? null : appConfig.getAdminUsername().trim();
        this.adminPassword = appConfig.getAdminPassword();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            throw new IllegalStateException("FATAL: ADMIN_USERNAME es obligatorio y no puede estar vacío");
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            throw new IllegalStateException("FATAL: ADMIN_PASSWORD es obligatorio y no puede estar vacío");
        }
        if (usuarioRepository.existsByUsername(adminUsername)) {
            return;
        }
        Rol adminRole = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("FATAL: el rol ADMIN no existe, revisar migración V1"));
        usuarioRepository.save(new Usuario(adminUsername,
                passwordEncoder.encode(adminPassword), adminRole));
    }
}