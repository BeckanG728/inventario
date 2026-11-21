package com.taller.producto.specification;

import com.taller.producto.entity.Producto;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

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

    // --- NUEVOS ---

    public static Specification<Producto> codigoContiene(String codigo) {
        String patron = "%" + codigo.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("codigo")), patron);
    }

    public static Specification<Producto> proveedorId(Long proveedorId) {
        return (root, query, cb) -> cb.equal(root.get("proveedor").get("id"), proveedorId);
    }

    public static Specification<Producto> unidadMedidaIgual(String unidadMedida) {
        String valor = unidadMedida.toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("unidadMedida")), valor);
    }

    public static Specification<Producto> activoIgual(boolean activo) {
        return (root, query, cb) -> cb.equal(root.get("activo"), activo);
    }

    /** RN302: crítico cuando stockActual <= stockMinimo. */
    public static Specification<Producto> stockCritico(boolean critico) {
        return (root, query, cb) -> critico
                ? cb.lessThanOrEqualTo(root.<Integer>get("stockActual"), root.<Integer>get("stockMinimo"))
                : cb.greaterThan(root.<Integer>get("stockActual"), root.<Integer>get("stockMinimo"));
    }
}