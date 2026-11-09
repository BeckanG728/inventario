package com.taller.inventario.consumo.service.impl;

import com.taller.auth.entity.Usuario;
import com.taller.common.exception.StockInsuficienteException;
import com.taller.inventario.consumo.domain.ConsumoItem;
import com.taller.inventario.movimiento.entity.MovimientoInventario;
import com.taller.inventario.movimiento.enums.TipoMovimiento;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.producto.entity.Producto;
import com.taller.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    private ConsumoStockServiceImpl service;

    private Producto producto(Long id, int stockActual, boolean activo) {
        Producto producto = new Producto("P-" + id, "Producto " + id, "UND", 0, null, null);
        producto.setId(id);
        producto.setStockActual(stockActual);
        producto.setActivo(activo);
        return producto;
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario("mecanico", "pwd", null);
        usuario.setId(7L);
        return usuario;
    }

    @Test
    void todosLosItemsValidos_descuentaStockYRegistraMovimientos() {
        Producto aceite = producto(1L, 10, true);
        Producto filtro = producto(2L, 5, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(aceite));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(filtro));
        when(productoRepository.findByIdAndActivoTrueForUpdate(1L)).thenReturn(Optional.of(aceite));
        when(productoRepository.findByIdAndActivoTrueForUpdate(2L)).thenReturn(Optional.of(filtro));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimientoInventario> movimientos = service.validarYDescontarStock(
                List.of(new ConsumoItem(1L, 2), new ConsumoItem(2L, 1)),
                usuario(), 99L, "Cambio de aceite");

        assertEquals(2, movimientos.size());
        assertEquals(8, aceite.getStockActual());
        assertEquals(4, filtro.getStockActual());
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
        Producto producto = producto(1L, 3, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.findByIdAndActivoTrueForUpdate(1L)).thenReturn(Optional.of(producto));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimientoInventario> movimientos = service.validarYDescontarStock(
                List.of(new ConsumoItem(1L, 3)), usuario(), 1L, "Uso");

        assertEquals(1, movimientos.size());
        assertEquals(0, producto.getStockActual());
        assertEquals(0, movimientos.get(0).getStockPosterior());
    }

    @Test
    void stockInsuficiente_noDescuentaNadaNiRegistraMovimientos() {
        Producto producto = producto(1L, 2, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, 3)), usuario(), 1L, "Uso"));

        assertTrue(ex.getMessage().contains("producto ID 1"));
        assertTrue(ex.getMessage().contains("solicitado 3"));
        assertTrue(ex.getMessage().contains("disponible 2"));
        assertEquals(2, producto.getStockActual());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
        verify(productoRepository, never()).findByIdAndActivoTrueForUpdate(any());
    }

    @Test
    void productoInexistente_seIdentificaElProducto() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(99L, 1)), usuario(), 1L, "Uso"));

        assertEquals("Producto ID 99 no encontrado o inactivo", ex.getMessage());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void productoInactivo_seIdentificaElProducto() {
        Producto producto = producto(5L, 10, false);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(5L, 1)), usuario(), 1L, "Uso"));

        assertEquals("Producto ID 5 no encontrado o inactivo", ex.getMessage());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void cantidadCero_esInvalidaAntesDeConsultarProductos() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, 0)), usuario(), 1L, "Uso"));

        assertEquals("La cantidad del producto ID 1 debe ser mayor que cero", ex.getMessage());
        verifyNoInteractions(productoRepository);
        verifyNoInteractions(movimientoInventarioRepository);
    }

    @Test
    void cantidadNegativa_esInvalidaAntesDeConsultarProductos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, -5)), usuario(), 1L, "Uso"));

        verifyNoInteractions(productoRepository);
    }

    @Test
    void listaVacia_esInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(List.of(), usuario(), 1L, "Uso"));

        assertEquals("Debe registrar al menos un producto", ex.getMessage());
        verifyNoInteractions(productoRepository);
    }

    @Test
    void listaNula_esInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validarYDescontarStock(null, usuario(), 1L, "Uso"));

        verifyNoInteractions(productoRepository);
    }

    @Test
    void productoRepetido_validaLaCantidadTotalAntesDeDescontar() {
        Producto producto = producto(1L, 5, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> service.validarYDescontarStock(
                        List.of(new ConsumoItem(1L, 3), new ConsumoItem(1L, 3)),
                        usuario(), 1L, "Uso"));

        assertTrue(ex.getMessage().contains("solicitado 6"));
        assertEquals(5, producto.getStockActual());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void productoRepetido_conStockSuficiente_descuentaCadaItem() {
        Producto producto = producto(1L, 5, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.findByIdAndActivoTrueForUpdate(1L)).thenReturn(Optional.of(producto));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MovimientoInventario> movimientos = service.validarYDescontarStock(
                List.of(new ConsumoItem(1L, 2), new ConsumoItem(1L, 2)),
                usuario(), 1L, "Uso");

        assertEquals(2, movimientos.size());
        assertEquals(1, producto.getStockActual());
        assertEquals(3, movimientos.get(1).getStockAnterior());
        assertEquals(1, movimientos.get(1).getStockPosterior());
    }
}
