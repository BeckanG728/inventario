package com.taller.producto.service.impl;

import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import com.taller.common.exception.DuplicateResourceException;
import com.taller.inventario.movimiento.repository.MovimientoInventarioRepository;
import com.taller.producto.dto.request.CreateProductoRequest;
import com.taller.producto.dto.request.UpdateProductoRequest;
import com.taller.producto.entity.Producto;
import com.taller.producto.mapper.ProductoMapper;
import com.taller.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RN101 — el código de producto es único y su incumplimiento responde 409 DUPLICATE_RESOURCE.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;
    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl service;

    @Test
    void crear_codigoDuplicado_lanzaDuplicateResourceException() {
        when(productoRepository.existsByCodigo("FIL-001")).thenReturn(true);
        CreateProductoRequest request = new CreateProductoRequest(
                " FIL-001 ", "Filtro de aceite", null, "UNIDAD", 10, 3L, 7L);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class, () -> service.crear(request));

        assertEquals("Ya existe un producto con el código: FIL-001", ex.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizar_codigoDeOtroProducto_lanzaDuplicateResourceException() {
        Producto producto = new Producto("FIL-001", "Filtro de aceite", "UNIDAD", 10, null, null);
        producto.setId(12L);
        when(productoRepository.findById(12L)).thenReturn(Optional.of(producto));
        when(movimientoInventarioRepository.existsByProductoId(12L)).thenReturn(false);
        when(productoRepository.existsByCodigoAndIdNot("FIL-002", 12L)).thenReturn(true);
        UpdateProductoRequest request = new UpdateProductoRequest(
                "FIL-002", "Filtro de aceite", null, "UNIDAD", 10, null, 3L, 7L);

        DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                () -> service.actualizar(12L, request));

        assertEquals("Ya existe un producto con el código: FIL-002", ex.getMessage());
    }
}
