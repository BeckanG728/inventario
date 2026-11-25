package com.taller.catalogo.producto.specification;

import com.taller.catalogo.producto.entity.Producto;
import com.taller.compartido.EstadoStock;
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

    // --- NUEVOS ---

    public static Specification<Producto> codigoContiene(String codigo) {
        String patron = "%" + codigo.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("codigo")), patron);
    }

    public static Specification<Producto> proveedorId(Long proveedorId) {
        return (root, query, cb) -> cb.equal(root.get("proveedor").get("id"), proveedorId);
    }

    public static Specification<Producto> unidadBaseIdIgual(Long unidadBaseId) {
        return (root, query, cb) -> cb.equal(root.get("unidadBase").get("id"), unidadBaseId);
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

    /** Clasificación operativa compartida por existencias y alertas. */
    public static Specification<Producto> estadoStock(EstadoStock estado) {
        return switch (estado) {
            case SIN_STOCK -> (root, query, cb) -> cb.equal(root.<Integer>get("stockActual"), 0);
            case BAJO -> (root, query, cb) -> cb.and(
                    cb.greaterThan(root.<Integer>get("stockActual"), 0),
                    cb.lessThanOrEqualTo(root.<Integer>get("stockActual"), root.<Integer>get("stockMinimo")));
            case NORMAL -> (root, query, cb) -> cb.greaterThan(
                    root.<Integer>get("stockActual"), root.<Integer>get("stockMinimo"));
        };
    }
}