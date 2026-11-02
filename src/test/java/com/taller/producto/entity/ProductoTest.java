package com.taller.producto.entity;

import com.taller.common.exception.StockInsuficienteException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * US-07 — Validación de stock y actualización automática (RN206).
 */
class ProductoTest {

    private Producto productoConStock(int stockActual) {
        Producto producto = new Producto("FILT-01", "Filtro Aceite", "UND", 10, null, null);
        producto.setStockActual(stockActual);
        return producto;
    }

    @Test
    void descontarStock_cantidadValida_actualizaElStock() {
        Producto producto = productoConStock(10);

        producto.descontarStock(4);

        assertEquals(6, producto.getStockActual());
    }

    @Test
    void descontarStock_cantidadIgualAlStock_loDejaEnCero() {
        Producto producto = productoConStock(3);

        producto.descontarStock(3);

        assertEquals(0, producto.getStockActual());
    }

    @Test
    void descontarStock_mayorQueElStock_lanzaExcepcionConMensajeEsperado() {
        Producto producto = productoConStock(2);

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> producto.descontarStock(3));

        assertEquals("Stock insuficiente. Quedaría negativo.", ex.getMessage());
        assertEquals(2, producto.getStockActual());
    }

    @Test
    void descontarStock_cantidadCero_esInvalida() {
        Producto producto = productoConStock(5);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> producto.descontarStock(0));

        assertEquals("La cantidad a descontar debe ser mayor que cero", ex.getMessage());
        assertEquals(5, producto.getStockActual());
    }

    @Test
    void descontarStock_cantidadNegativa_esInvalida() {
        Producto producto = productoConStock(5);

        assertThrows(IllegalArgumentException.class, () -> producto.descontarStock(-1));
        assertEquals(5, producto.getStockActual());
    }

    @Test
    void descontarStock_variasVeces_acumulaElDescuento() {
        Producto producto = productoConStock(10);

        producto.descontarStock(4);
        producto.descontarStock(5);

        assertEquals(1, producto.getStockActual());
        assertThrows(StockInsuficienteException.class, () -> producto.descontarStock(2));
        assertEquals(1, producto.getStockActual());
    }
}
