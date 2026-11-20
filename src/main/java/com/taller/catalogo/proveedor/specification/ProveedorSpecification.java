package com.taller.catalogo.proveedor.specification;

import com.taller.catalogo.proveedor.entity.Proveedor;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

public final class ProveedorSpecification {

    private ProveedorSpecification() {
    }

    public static Specification<Proveedor> razonSocialContiene(String razonSocial) {
        String patron = "%" + razonSocial.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("razonSocial")), patron);
    }

    public static Specification<Proveedor> rucContiene(String ruc) {
        return (root, query, cb) -> cb.like(root.get("ruc"), "%" + ruc + "%");
    }

    public static Specification<Proveedor> activoIgual(boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }
}