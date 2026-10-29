package com.taller.producto.service.impl;

import com.taller.catalogo.entity.Categoria;
import com.taller.catalogo.entity.Proveedor;
import com.taller.catalogo.repository.CategoriaRepository;
import com.taller.catalogo.repository.ProveedorRepository;
import com.taller.movimiento.repository.MovimientoInventarioRepository;
import com.taller.producto.dto.request.CreateProductoRequest;
import com.taller.producto.dto.request.UpdateProductoRequest;
import com.taller.producto.dto.response.ProductoResponse;
import com.taller.producto.entity.Producto;
import com.taller.producto.exception.ProductoNotFoundException;
import com.taller.producto.mapper.ProductoMapper;
import com.taller.producto.repository.ProductoRepository;
import com.taller.producto.service.ProductoService;
import com.taller.producto.specification.ProductoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private static final String MENSAJE_SOLO_CAMPOS_PERMITIDOS =
            "El producto tiene movimientos registrados: solo se puede editar nombre, descripción y unidad de medida";
    private static final String PREFIJO_SKU = "CP-";
    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional
    public ProductoResponse crear(CreateProductoRequest request) {
        String codigo = request.codigo().trim();

        if (productoRepository.existsByCodigo(codigo)) {
            throw new IllegalArgumentException("Ya existe un producto con el código: " + codigo);
        }

        Categoria categoria = obtenerCategoriaActiva(request.idCategoria());
        Proveedor proveedor = obtenerProveedorActivo(request.idProveedor());

        Producto producto = new Producto(
                codigo,
                request.nombre().trim(),
                request.unidadMedida().trim(),
                request.stockMinimo(),
                categoria,
                proveedor);

        producto.setDescripcion(request.descripcion());
        producto.setStockActual(0);
        producto.setActivo(true);

        Producto guardado = productoRepository.save(producto);
        guardado.setSku(generarSku(guardado.getId()));

        return productoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(int page, int size, String nombre, Long categoriaId) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO),
                Sort.by("nombre", "id"));
        String filtroNombre = (nombre == null || nombre.isBlank()) ? null : nombre.trim();

        Specification<Producto> filtros = Specification.unrestricted();
        if (filtroNombre != null) {
            filtros = filtros.and(ProductoSpecification.nombreContiene(filtroNombre));
        }
        if (categoriaId != null) {
            filtros = filtros.and(ProductoSpecification.categoriaId(categoriaId));
        }

        Page<Producto> productos = productoRepository.findAll(filtros, pageable);

        return productos.map(productoMapper::toResponse);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, UpdateProductoRequest request) {
        Producto producto = obtenerProducto(id);

        if (movimientoInventarioRepository.existsByProductoId(id)) {
            validarEdicionConHistorial(producto, request);
        }

        aplicarCambios(producto, request);

        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse desactivar(Long id) {
        Producto producto = obtenerProducto(id);

        if (movimientoInventarioRepository.existsByProductoId(id)) {
            throw new IllegalArgumentException(
                    "No se puede desactivar: el producto tiene movimientos registrados. ");
        }

        producto.setActivo(false);
        return productoMapper.toResponse(producto);
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private Categoria obtenerCategoriaActiva(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        if (!categoria.isActivo()) {
            throw new IllegalArgumentException("Categoría inactiva");
        }
        return categoria;
    }

    private Proveedor obtenerProveedorActivo(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        if (!proveedor.isActivo()) {
            throw new IllegalArgumentException("Proveedor inactivo");
        }
        return proveedor;
    }

    private void validarEdicionConHistorial(Producto producto, UpdateProductoRequest request) {
        if (!request.codigo().trim().equals(producto.getCodigo())) {
            throw new IllegalArgumentException(MENSAJE_SOLO_CAMPOS_PERMITIDOS);
        }
        if (!request.idCategoria().equals(producto.getCategoria().getId())) {
            throw new IllegalArgumentException(MENSAJE_SOLO_CAMPOS_PERMITIDOS);
        }
        if (!request.idProveedor().equals(producto.getProveedor().getId())) {
            throw new IllegalArgumentException(MENSAJE_SOLO_CAMPOS_PERMITIDOS);
        }
        if (!request.stockMinimo().equals(producto.getStockMinimo())) {
            throw new IllegalArgumentException(
                    "No se puede modificar el stock mínimo: el producto tiene movimientos registrados");
        }
        if (request.activo() != null && !request.activo().equals(producto.isActivo())) {
            throw new IllegalArgumentException(
                    "No se puede modificar el estado activo: el producto tiene movimientos registrados");
        }
    }

    private void aplicarCambios(Producto producto, UpdateProductoRequest request) {
        String codigo = request.codigo().trim();
        if (!codigo.equals(producto.getCodigo())) {
            if (productoRepository.existsByCodigoAndIdNot(codigo, producto.getId())) {
                throw new IllegalArgumentException("Ya existe un producto con el código: " + codigo);
            }
            producto.setCodigo(codigo);
        }
        if (!request.idCategoria().equals(producto.getCategoria().getId())) {
            producto.setCategoria(obtenerCategoriaActiva(request.idCategoria()));
        }
        if (!request.idProveedor().equals(producto.getProveedor().getId())) {
            producto.setProveedor(obtenerProveedorActivo(request.idProveedor()));
        }

        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setUnidadMedida(request.unidadMedida().trim());
        producto.setStockMinimo(request.stockMinimo());
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
    }

    private String generarSku(Long id) {
        return PREFIJO_SKU + String.format("%06d", id);
    }
}
