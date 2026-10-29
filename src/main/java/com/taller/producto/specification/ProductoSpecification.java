package com.taller.producto.specification;

import com.taller.producto.entity.Producto;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class ProductoSpecification {

    private ProductoSpecification() {
    }

    public static Specification<Producto> nombreContiene(String nombre) {
        String patron = "%" + nombre.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), patron);
    }

    public static Specification<Producto> categoriaId(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }
}
