package com.taller.catalogo.producto.service.impl;

import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import com.taller.catalogo.producto.dto.request.CreateProductoRequest;
import com.taller.catalogo.producto.dto.request.UpdateProductoRequest;
import com.taller.catalogo.producto.entity.Producto;
import com.taller.catalogo.producto.mapper.ProductoMapper;
import com.taller.catalogo.producto.repository.ProductoRepository;
import com.taller.inventario.api.HistorialMovimientos;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RN101 — el código de producto lo genera el backend con el patrón PRD-nnnn.
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
    private HistorialMovimientos historialMovimientos;
    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl service;

    private Categoria categoria() {
        return new Categoria("Filtros", null);
    }

    private Proveedor proveedor() {
        return new Proveedor("20123456789", "Proveedor Demo", null, null, null, null);
    }

    @Test
    void crear_generaCodigoConElCorrelativoSiguiente() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria()));
        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor()));
        when(productoRepository.findMaxCodigo()).thenReturn("PRD-0012");
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductoRequest request =
                new CreateProductoRequest("Filtro de aceite", null, "UNIDAD", 10, 3L, 7L);
        service.crear(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals("PRD-0013", captor.getValue().getCodigo());
    }

    @Test
    void actualizar_noModificaElCodigoGenerado() {
        Producto producto = new Producto("PRD-0001", "Filtro", "UNIDAD", 10, categoria(), proveedor());
        when(productoRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(producto));
        when(historialMovimientos.existePorProducto(12L)).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria()));
        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor()));

        UpdateProductoRequest request =
                new UpdateProductoRequest("Filtro nuevo", null, "UNIDAD", 10, null, 3L, 7L);
        service.actualizar(12L, request);

        assertEquals("PRD-0001", producto.getCodigo());
    }
}
