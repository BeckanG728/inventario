package com.taller.inventario.consumo.service.impl;

import com.taller.catalogo.api.ProductoStock;
import com.taller.catalogo.api.ProductoStock.ResultadoStock;
import com.taller.compartido.exception.StockInsuficienteException;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.unidad.service.ConversorUnidades;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * US-12 — Validación de stock suficiente y descuento atómico (RN207).
 */
@ExtendWith(MockitoExtension.class)
class ConsumoStockServiceImplTest {

    @Mock
    private ProductoStock productoStock;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Spy
    private ConversorUnidades conversorUnidades = new ConversorUnidades();

    @InjectMocks
    private ConsumoStockServiceImpl service;

    private ProductoResponse producto(Long id, int stockActual, boolean activo) {
        return new ProductoResponse(
                id, "P-" + id, "Producto " + id, null,
                stockActual, 0, false, null, activo, null, null, null, null,
                null, "UND", null, null, null, null);
    }

    @Test
    void todosLosItemsValidos_descuentaStockYRegistraMovimientos() {
        ProductoResponse aceite = producto(1L, 10, true);
        ProductoResponse filtro = producto(2L, 5, true);
        when(productoStock.bloquearParaDescuento(List.of(1L, 2L))).thenReturn(List.of(aceite, filtro));
        when(productoStock.descontarStock(1L, 2)).thenReturn(new ResultadoStock(aceite, 10, 8));
        when(productoStock.descontarStock(2L, 1)).thenReturn(new ResultadoStock(filtro, 5, 4));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimientoInventario> movimientos = service.validarYDescontarStock(
                List.of(new ConsumoItem(1L, 2), new ConsumoItem(2L, 1)),
                7L, 99L, "Cambio de aceite");

        assertEquals(2, movimientos.size());
        assertEquals(TipoMovimiento.CONSUMO, movimientos.get(0).getTipo());
        assertEquals(10, movimientos.get(0).getStockAnterior());
        assertEquals(8, movimientos.get(0).getStockPosterior());
        assertEquals(2, movimientos.get(0).getCantidad());
        assertEquals(99L, movimientos.get(0).getConsumoId());
        assertEquals("Cambio de aceite", movimientos.get(0).getMotivo());
        verify(movimientoInventarioRepository, times(2)).save(any(MovimientoInventario.class));
    }

    @Test
    void consumoExactoDelStock_disponibleLoDejaEnCero() {
        ProductoResponse producto = producto(1L, 3, true);
        when(productoStock.bloquearParaDescuento(List.of(1L))).thenReturn(List.of(producto));
        when(productoStock.descontarStock(1L, 3)).thenReturn(new ResultadoStock(producto, 3, 0));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimientoInventario> movimientos = service.validarYDescontarStock(
                List.of(new ConsumoItem(1L, 3)), 7L, 1L, "Uso");

        assertEquals(1, movimientos.size());
        assertEquals(0, movimientos.get(0).getStockPosterior());
    }

    @Test
    void stockInsuficiente_noDescuentaNadaNiRegistraMovimientos() {
        when(productoStock.bloquearParaDescuento(List.of(1L))).thenReturn(List.of(producto(1L, 2, true)));

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, 3)), 7L, 1L, "Uso"));

        assertTrue(ex.getMessage().contains("producto ID 1"));
        assertTrue(ex.getMessage().contains("solicitado 3"));
        assertTrue(ex.getMessage().contains("disponible 2"));
        verify(productoStock, never()).descontarStock(any(), anyInt());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void productoInexistente_seIdentificaElProducto() {
        when(productoStock.bloquearParaDescuento(List.of(99L))).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(99L, 1)), 7L, 1L, "Uso"));

        assertEquals("Producto ID 99 no encontrado o inactivo", ex.getMessage());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void productoInactivo_seIdentificaElProducto() {
        when(productoStock.bloquearParaDescuento(List.of(5L))).thenReturn(List.of(producto(5L, 10, false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(5L, 1)), 7L, 1L, "Uso"));

        assertEquals("Producto ID 5 no encontrado o inactivo", ex.getMessage());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void cantidadCero_esInvalidaAntesDeConsultarProductos() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, 0)), 7L, 1L, "Uso"));

        assertEquals("La cantidad del producto ID 1 debe ser mayor que cero", ex.getMessage());
        verifyNoInteractions(productoStock);
        verifyNoInteractions(movimientoInventarioRepository);
    }

    @Test
    void cantidadNegativa_esInvalidaAntesDeConsultarProductos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, -5)), 7L, 1L, "Uso"));

        verifyNoInteractions(productoStock);
    }

    @Test
    void listaVacia_esInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(List.of(), 7L, 1L, "Uso"));

        assertEquals("Debe registrar al menos un producto", ex.getMessage());
        verifyNoInteractions(productoStock);
    }

    @Test
    void listaNula_esInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(null, 7L, 1L, "Uso"));

        verifyNoInteractions(productoStock);
    }

    @Test
    void productoRepetido_validaLaCantidadTotalAntesDeDescontar() {
        when(productoStock.bloquearParaDescuento(List.of(1L))).thenReturn(List.of(producto(1L, 5, true)));

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, 3), new ConsumoItem(1L, 3)),
                        7L, 1L, "Uso"));

        assertTrue(ex.getMessage().contains("solicitado 6"));
        verify(productoStock, never()).descontarStock(any(), anyInt());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void productoRepetido_conStockSuficiente_descuentaCadaItem() {
        ProductoResponse producto = producto(1L, 5, true);
        when(productoStock.bloquearParaDescuento(List.of(1L))).thenReturn(List.of(producto));
        when(productoStock.descontarStock(1L, 2))
                .thenReturn(new ResultadoStock(producto, 5, 3), new ResultadoStock(producto, 3, 1));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimientoInventario> movimientos = service.validarYDescontarStock(
                List.of(new ConsumoItem(1L, 2), new ConsumoItem(1L, 2)),
                7L, 1L, "Uso");

        assertEquals(2, movimientos.size());
        assertEquals(3, movimientos.get(1).getStockAnterior());
        assertEquals(1, movimientos.get(1).getStockPosterior());
    }

    @Test
    void itemsEnOrdenInverso_bloqueanLosProductosEnOrdenAscendente() {
        ProductoResponse aceite = producto(1L, 10, true);
        ProductoResponse filtro = producto(2L, 5, true);
        when(productoStock.bloquearParaDescuento(List.of(1L, 2L))).thenReturn(List.of(filtro, aceite));
        when(productoStock.descontarStock(2L, 1)).thenReturn(new ResultadoStock(filtro, 5, 4));
        when(productoStock.descontarStock(1L, 2)).thenReturn(new ResultadoStock(aceite, 10, 8));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.validarYDescontarStock(
                List.of(new ConsumoItem(2L, 1), new ConsumoItem(1L, 2)), 7L, 1L, "Uso");

        verify(productoStock).bloquearParaDescuento(List.of(1L, 2L));
    }
}
