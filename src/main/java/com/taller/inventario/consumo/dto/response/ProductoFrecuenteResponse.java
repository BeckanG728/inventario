package com.taller.inventario.consumo.dto.response;

import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.compartido.EstadoStock;

/**
 * Producto activo frecuentemente consumido, pensado para el formulario de registro de consumos.
 * Incluye los datos que la tarjeta de selección ya muestra, más la frecuencia en el periodo.
 */
public record ProductoFrecuenteResponse(
        Long productoId,
        String codigo,
        String nombre,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockCritico,
        EstadoStock estadoStock,
        String categoriaNombre,
        long vecesConsumido,
        long unidadesConsumidas,
        String unidadBaseCodigo,
        String unidadPresentacionCodigo,
        Integer factorPresentacion
) {

    public static ProductoFrecuenteResponse desde(ProductoResponse producto,
                                                  long vecesConsumido, long unidadesConsumidas) {
        return new ProductoFrecuenteResponse(
                producto.id(),
                producto.codigo(),
                producto.nombre(),
                producto.stockActual(),
                producto.stockMinimo(),
                producto.stockCritico(),
                EstadoStock.de(producto.stockActual(), producto.stockMinimo()),
                producto.categoriaNombre(),
                vecesConsumido,
                unidadesConsumidas,
                producto.unidadBaseCodigo(),
                producto.unidadPresentacionCodigo(),
                producto.factorPresentacion());
    }
}
