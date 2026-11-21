package com.taller.seguridad.api.impl;

import com.taller.seguridad.api.PreparacionSeguridad;
import com.taller.seguridad.usuario.entity.Rol;
import com.taller.seguridad.usuario.entity.Usuario;
import com.taller.seguridad.usuario.repository.RolRepository;
import com.taller.seguridad.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PreparacionSeguridadImpl implements PreparacionSeguridad {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuariosDemo asegurarUsuariosDemo(String jefe, List<String> mecanicos, String password) {
        UsuarioDemo jefeAsegurado = asegurar(jefe, "JEFE_TALLER", password);
        List<UsuarioDemo> mecanicosAsegurados = mecanicos.stream()
                .map(usuario -> asegurar(usuario, "MECANICO", password))
                .toList();
        return new UsuariosDemo(jefeAsegurado, mecanicosAsegurados);
    }

    private UsuarioDemo asegurar(String username, String rolNombre, String password) {
        String nombre = username.trim();
        return usuarioRepository.findByUsername(nombre)
                .map(usuario -> new UsuarioDemo(usuario.getId(), usuario.getUsername()))
                .orElseGet(() -> {
                    Rol rol = rolRepository.findByNombre(rolNombre)
                            .orElseThrow(() -> new IllegalStateException(
                                    "FATAL: el rol " + rolNombre + " no existe, revisar migración V1"));
                    var guardado = usuarioRepository.save(
                            new Usuario(nombre, passwordEncoder.encode(password), rol));
                    return new UsuarioDemo(guardado.getId(), guardado.getUsername());
                });
    }
}
