package com.taller.seguridad.api;

import java.util.List;

/**
 * Operación de preparación de seguridad: asegurar los usuarios ficticios del perfil {@code dev}.
 *
 * <p>Es una operación de seeding, no de negocio: permite crear usuarios con una contraseña
 * indicada por configuración, algo que el registro público prohíbe.</p>
 */
public interface PreparacionSeguridad {

    record UsuarioDemo(Long id, String username) {
    }

    record UsuariosDemo(UsuarioDemo jefe, List<UsuarioDemo> mecanicos) {
    }

    UsuariosDemo asegurarUsuariosDemo(String jefe, List<String> mecanicos, String password);
}
