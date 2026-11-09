package com.taller.inventario.consumo.domain;

/**
 * Ítem interno de un consumo ya validado en formato.
 */
public record ConsumoItem(Long productoId, int cantidad) {
}
