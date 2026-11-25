package com.taller.inventario.consumo.domain;

import java.math.BigDecimal;

/**
 * Ítem interno de un consumo ya validado en formato.
 *
 * <p>{@code cantidad} se expresa en la unidad indicada por {@code unidadRegistro}; cuando se usa
 * {@code cantidadExacta} (longitud en metros, por ejemplo) prevalece sobre {@code cantidad}.</p>
 */
public record ConsumoItem(Long productoId, int cantidad, String unidadRegistro,
                          Integer factorAplicado, BigDecimal cantidadExacta) {

    public ConsumoItem(Long productoId, int cantidad) {
        this(productoId, cantidad, null, null, null);
    }
}
