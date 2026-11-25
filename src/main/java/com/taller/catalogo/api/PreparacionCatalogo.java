package com.taller.catalogo.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * Operaciones de preparación del catálogo para los datos ficticios del perfil {@code dev}.
 *
 * <p>Permiten fijar estados históricos (productos inactivos y stock mínimo forzado) que las
 * operaciones públicas no pueden producir.</p>
 */
public interface PreparacionCatalogo {

    record CategoriaDemo(String nombre, String descripcion) {
    }

    record ProveedorDemo(String razonSocial, String contacto, String telefono,
                         String correo, String direccion) {
    }

    record ProductoDemo(String nombre, String descripcion,
                        Integer stockMinimo, BigDecimal costoUltimaCompra,
                        Long categoriaId, Long proveedorId,
                        String unidadBaseCodigo, String unidadPresentacionCodigo, Integer factorPresentacion) {
    }

    record ProductoCreado(Long id, Integer stockMinimo, BigDecimal costoUltimaCompra, Long proveedorId) {
    }

    record EstadoProductoDemo(Long id, Integer stockActual, Integer stockMinimo, boolean activo) {
    }

    List<Long> crearCategorias(List<CategoriaDemo> categorias);

    List<Long> crearProveedores(List<ProveedorDemo> proveedores);

    List<ProductoCreado> crearProductos(List<ProductoDemo> productos);

    void aplicarEstado(List<EstadoProductoDemo> estados);

    boolean hayProductos();
}
