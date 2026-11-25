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
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.catalogo.unidad.repository.UnidadRepository;
import com.taller.inventario.api.HistorialMovimientos;
import java.util.Optional;
import java.util.Set;
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
 * RN101 — el código de producto lo genera el backend con el patrón AAA-nnnn
 * (AAA = siglas del nombre de la categoría).
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
    @Mock
    private UnidadRepository unidadRepository;

    @InjectMocks
    private ProductoServiceImpl service;

    private Unidad unidad() {
        return new Unidad("UND", "Unidad", false);
    }

    private Categoria categoria() {
        Categoria categoria = new Categoria("Filtros", null);
        categoria.reemplazarUnidades(Set.of(unidad()));
        return categoria;
    }

    private Proveedor proveedor() {
        return new Proveedor("20123456789", "Proveedor Demo", null, null, null, null);
    }

    @Test
    void crear_generaCodigoConElCorrelativoSiguiente() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria()));
        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor()));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad()));
        when(productoRepository.findMaxCodigoByPrefijo("FIL")).thenReturn("FIL-0012");
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProductoRequest request = new CreateProductoRequest(
                "Filtro de aceite", null, 1L, null, null, 10, 3L, 7L);
        service.crear(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        assertEquals("FIL-0013", captor.getValue().getCodigo());
        assertEquals("UND", captor.getValue().getUnidadBase().getCodigo());
    }

    @Test
    void actualizar_noModificaElCodigoGenerado() {
        Unidad unidad = unidad();
        Categoria categoria = categoria();
        Producto producto = new Producto("FIL-0001", "Filtro", unidad, 10, categoria, proveedor());
        when(productoRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(producto));
        when(historialMovimientos.existePorProducto(12L)).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(proveedorRepository.findById(7L)).thenReturn(Optional.of(proveedor()));
        when(unidadRepository.findById(1L)).thenReturn(Optional.of(unidad));

        UpdateProductoRequest request = new UpdateProductoRequest(
                "Filtro nuevo", null, 1L, null, null, 10, null, 3L, 7L);
        service.actualizar(12L, request);

        assertEquals("FIL-0001", producto.getCodigo());
    }
}
