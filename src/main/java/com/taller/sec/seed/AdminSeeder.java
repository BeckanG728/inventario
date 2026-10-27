package com.taller.sec.seed;

import com.taller.sec.entity.Rol;
import com.taller.sec.entity.Usuario;
import com.taller.sec.repository.RolRepository;
import com.taller.sec.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${admin.username}") String adminUsername,
                       @Value("${admin.email}") String adminEmail,
                       @Value("${admin.password}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByEmail(adminEmail)) {
            return;
        }
        Rol adminRole = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("FATAL: el rol ADMIN no existe, revisar migración V1"));
        usuarioRepository.save(new Usuario(adminUsername, adminEmail,
                passwordEncoder.encode(adminPassword), adminRole));
    }
}