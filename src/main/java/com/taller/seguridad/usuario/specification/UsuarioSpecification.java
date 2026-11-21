package com.taller.seguridad.usuario.specification;

import com.taller.seguridad.usuario.entity.Usuario;
import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> usernameContiene(String username) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }

    public static Specification<Usuario> rolIgual(String role) {
        return (root, query, cb) -> cb.equal(root.get("rol").get("nombre"), role);
    }

    public static Specification<Usuario> activoIgual(Boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}
