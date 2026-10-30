package com.taller.movimiento.dto.response;

import com.taller.producto.dto.response.ProductoResponse;

/**
 * Respuesta de una entrada: el movimiento creado y el nuevo estado del producto.
 */
public record EntradaResponse(
        MovimientoResponse movimiento,
        ProductoResponse producto
) {
}
