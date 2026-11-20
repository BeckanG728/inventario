package com.taller.catalogo.categoria.specification;

import com.taller.catalogo.categoria.entity.Categoria;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class CategoriaSpecification {

    private CategoriaSpecification() {
    }

    public static Specification<Categoria> nombreContiene(String nombre) {
        String patron = "%" + nombre.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron);
    }

    public static Specification<Categoria> activoIgual(boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}