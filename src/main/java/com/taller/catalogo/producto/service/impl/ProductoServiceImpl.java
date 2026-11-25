package com.taller.catalogo.producto.service.impl;

import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.catalogo.unidad.repository.UnidadRepository;
import com.taller.inventario.api.HistorialMovimientos;
import com.taller.catalogo.producto.dto.request.CreateProductoRequest;
import com.taller.catalogo.producto.dto.request.ProductoFiltro;
import com.taller.catalogo.producto.dto.request.UpdateProductoRequest;
import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.catalogo.producto.entity.Producto;
import com.taller.catalogo.producto.exception.ProductoNotFoundException;
import com.taller.catalogo.producto.mapper.ProductoMapper;
import com.taller.catalogo.producto.repository.ProductoRepository;
import com.taller.catalogo.producto.service.CodigoGenerator;
import com.taller.catalogo.producto.service.ProductoService;
import com.taller.catalogo.producto.specification.ProductoSpecification;
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
    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final HistorialMovimientos historialMovimientos;
    private final ProductoMapper productoMapper;
    private final UnidadRepository unidadRepository;

    @Override
    @Transactional
    public ProductoResponse crear(CreateProductoRequest request) {
        Categoria categoria = obtenerCategoriaActiva(request.idCategoria());
        Proveedor proveedor = obtenerProveedorActivo(request.idProveedor());

        productoRepository.bloquearAlta();
        String prefijo = CodigoGenerator.prefijo(categoria.getNombre());
        String maxCodigo = productoRepository.findMaxCodigoByPrefijo(prefijo);
        String codigo = CodigoGenerator.generar(
                prefijo, Math.incrementExact(CodigoGenerator.correlativoDe(maxCodigo)));

        Unidad unidadBase = resolverUnidadBase(request.unidadBaseId(), categoria);
        Unidad presentacion = resolverPresentacion(request.unidadPresentacionId(), categoria);
        if (presentacion != null && request.factorPresentacion() == null) {
            throw new IllegalArgumentException("El factor de presentación es obligatorio cuando se indica una presentación");
        }

        Producto producto = new Producto(
                codigo,
                request.nombre().trim(),
                unidadBase,
                request.stockMinimo(),
                categoria,
                proveedor);

        producto.setDescripcion(request.descripcion());
        producto.setStockActual(0);
        producto.setActivo(true);
        producto.setUnidadPresentacion(presentacion);
        producto.setFactorPresentacion(request.factorPresentacion());

        Producto guardado = productoRepository.save(producto);
        return productoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(int page, int size, ProductoFiltro filtro) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.clamp(size, 1, TAMANO_PAGINA_MAXIMO),
                Sort.by("nombre", "id"));

        Specification<Producto> filtros = Specification.unrestricted();

        String nombre = textoONull(filtro.nombre());
        if (nombre != null) {
            filtros = filtros.and(ProductoSpecification.nombreContiene(nombre));
        }
        if (filtro.categoriaId() != null) {
            filtros = filtros.and(ProductoSpecification.categoriaId(filtro.categoriaId()));
        }
        String codigo = textoONull(filtro.codigo());
        if (codigo != null) {
            filtros = filtros.and(ProductoSpecification.codigoContiene(codigo));
        }
        if (filtro.proveedorId() != null) {
            filtros = filtros.and(ProductoSpecification.proveedorId(filtro.proveedorId()));
        }
        if (filtro.unidadBaseId() != null) {
            filtros = filtros.and(ProductoSpecification.unidadBaseIdIgual(filtro.unidadBaseId()));
        }
        if (filtro.activo() != null) {
            filtros = filtros.and(ProductoSpecification.activoIgual(filtro.activo()));
        }
        if (filtro.stockCritico() != null) {
            filtros = filtros.and(ProductoSpecification.stockCritico(filtro.stockCritico()));
        }

        return productoRepository.findAll(filtros, pageable).map(productoMapper::toResponse);
    }

    private static String textoONull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, UpdateProductoRequest request) {
        Producto producto = obtenerProducto(id);

        if (historialMovimientos.existePorProducto(id)) {
            validarEdicionConHistorial(producto, request);
        }

        aplicarCambios(producto, request);

        return productoMapper.toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse desactivar(Long id) {
        Producto producto = obtenerProducto(id);

        // RN108: la eliminación física no está permitida. La desactivación lógica
        // conserva el historial de movimientos, por lo que puede aplicarse siempre.
        producto.setActivo(false);
        return productoMapper.toResponse(producto);
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findByIdForUpdate(id)
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

    private Unidad resolverUnidadBase(Long id, Categoria categoria) {
        if (id == null) {
            throw new IllegalArgumentException("La unidad base es obligatoria");
        }
        Unidad unidad = resolverUnidad(id);
        validarUnidadDeCategoria(unidad, categoria);
        return unidad;
    }

    private Unidad resolverPresentacion(Long id, Categoria categoria) {
        if (id == null) {
            return null;
        }
        Unidad unidad = resolverUnidad(id);
        validarUnidadDeCategoria(unidad, categoria);
        return unidad;
    }

    private Unidad resolverUnidad(Long id) {
        Unidad unidad = unidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidad no encontrada: " + id));
        if (!unidad.isActivo()) {
            throw new IllegalArgumentException("Unidad inactiva: " + unidad.getCodigo());
        }
        return unidad;
    }

    private void validarUnidadDeCategoria(Unidad unidad, Categoria categoria) {
        if (!categoria.admiteUnidad(unidad)) {
            throw new IllegalArgumentException("La unidad " + unidad.getCodigo()
                    + " no es válida para la categoría " + categoria.getNombre());
        }
    }

    private void validarEdicionConHistorial(Producto producto, UpdateProductoRequest request) {
        if (!request.idCategoria().equals(producto.getCategoria().getId())) {
            throw new IllegalArgumentException(MENSAJE_SOLO_CAMPOS_PERMITIDOS);
        }
        if (!unidadBaseSolicitadaEsActual(producto, request)) {
            throw new IllegalArgumentException("No se puede cambiar la unidad base: el producto tiene movimientos registrados");
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

    private boolean unidadBaseSolicitadaEsActual(Producto producto, UpdateProductoRequest request) {
        Unidad actual = producto.getUnidadBase();
        if (actual == null) {
            return request.unidadBaseId() == null;
        }
        return request.unidadBaseId() != null && request.unidadBaseId().equals(actual.getId());
    }

    private void aplicarCambios(Producto producto, UpdateProductoRequest request) {
        Categoria categoria = producto.getCategoria();
        if (!request.idCategoria().equals(categoria.getId())) {
            categoria = obtenerCategoriaActiva(request.idCategoria());
            producto.setCategoria(categoria);
        }
        if (!request.idProveedor().equals(producto.getProveedor().getId())) {
            producto.setProveedor(obtenerProveedorActivo(request.idProveedor()));
        }

        Unidad unidadBase = resolverUnidadBase(request.unidadBaseId(), categoria);
        producto.setUnidadBase(unidadBase);

        Unidad presentacion = resolverPresentacion(request.unidadPresentacionId(), categoria);
        if (presentacion != null && request.factorPresentacion() == null) {
            throw new IllegalArgumentException("El factor de presentación es obligatorio cuando se indica una presentación");
        }
        producto.setUnidadPresentacion(presentacion);
        producto.setFactorPresentacion(request.factorPresentacion());

        producto.setNombre(request.nombre().trim());
        producto.setDescripcion(request.descripcion());
        producto.setStockMinimo(request.stockMinimo());
        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }
    }

}
