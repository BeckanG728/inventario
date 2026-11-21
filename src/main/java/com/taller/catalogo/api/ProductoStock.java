package com.taller.catalogo.api;

import com.taller.catalogo.producto.dto.response.ProductoResponse;

import java.math.BigDecimal;

/**
 * Operaciones de stock que el catálogo ofrece a otros dominios.
 *
 * <p>La implementación oculta el repositorio, el bloqueo optimista/pesimista y las validaciones
 * de {@code Producto}. Participa en la transacción del llamador (REQUIRED): la operación de
 * inventario y la actualización del producto son atómicas.</p>
 */
public interface ProductoStock {

    /** Estado del producto antes y después de una operación de stock. */
    record ResultadoStock(
            ProductoResponse producto,
            Integer stockAnterior,
            Integer stockPosterior
    ) {
    }

    /** Resultado de una entrada: incluye el motivo ya resuelto con la razón social del proveedor. */
    record ResultadoEntrada(
            ProductoResponse producto,
            Integer stockAnterior,
            Integer stockPosterior,
            String motivo
    ) {
    }

    ResultadoStock descontarStock(Long productoId, int cantidad);

    ResultadoStock fijarStock(Long productoId, int cantidad);

    ProductoResponse fijarStockMinimo(Long productoId, Integer stockMinimo);

    ResultadoEntrada aplicarEntrada(Long productoId, Long proveedorId, int cantidad,
                                    BigDecimal costoUnitario);
}
