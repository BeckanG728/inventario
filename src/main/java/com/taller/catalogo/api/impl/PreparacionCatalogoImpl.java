package com.taller.catalogo.api.impl;

import com.taller.catalogo.api.PreparacionCatalogo;
import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.producto.entity.Producto;
import com.taller.catalogo.producto.repository.ProductoRepository;
import com.taller.catalogo.producto.service.CodigoGenerator;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import com.taller.catalogo.unidad.entity.Unidad;
import com.taller.catalogo.unidad.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PreparacionCatalogoImpl implements PreparacionCatalogo {

    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UnidadRepository unidadRepository;

    @Override
    @Transactional
    public List<Long> crearCategorias(List<CategoriaDemo> categorias) {
        List<Long> ids = new ArrayList<>(categorias.size());
        for (CategoriaDemo categoria : categorias) {
            String nombre = categoria.nombre();
            // V10 ya siembra las categorías base y sus unidades: se reutilizan en vez de duplicarlas.
            Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCase(nombre);
            if (existente.isPresent()) {
                ids.add(existente.get().getId());
                continue;
            }
            ids.add(categoriaRepository.save(
                    new Categoria(nombreCategoriaUnico(nombre), categoria.descripcion())).getId());
        }
        return ids;
    }

    @Override
    @Transactional
    public void asociarUnidades(List<CategoriaUnidadDemo> asociaciones) {
        for (CategoriaUnidadDemo asociacion : asociaciones) {
            Categoria categoria = categoriaRepository.findById(asociacion.categoriaId())
                    .orElseThrow(() -> new IllegalStateException("Categoría demo no encontrada"));
            Unidad unidad = unidadRepository.findByCodigoIgnoreCase(asociacion.unidadCodigo())
                    .orElseThrow(() -> new IllegalStateException(
                            "Unidad de negocio no encontrada: " + asociacion.unidadCodigo()));
            if (!categoria.getUnidades().contains(unidad)) {
                java.util.Set<Unidad> unidades = new java.util.LinkedHashSet<>(categoria.getUnidades());
                unidades.add(unidad);
                categoria.reemplazarUnidades(unidades);
            }
        }
    }

    @Override
    @Transactional
    public List<Long> crearProveedores(List<ProveedorDemo> proveedores) {
        List<Long> ids = new ArrayList<>(proveedores.size());
        int contador = 1;
        for (ProveedorDemo proveedor : proveedores) {
            String ruc;
            do {
                ruc = String.format(Locale.ROOT, "20%09d", contador++);
            } while (proveedorRepository.existsByRuc(ruc));

            ids.add(proveedorRepository.save(new Proveedor(
                    ruc,
                    proveedor.razonSocial(),
                    proveedor.contacto(),
                    proveedor.telefono(),
                    proveedor.correo(),
                    proveedor.direccion())).getId());
        }
        return ids;
    }

    @Override
    @Transactional
    public List<ProductoCreado> crearProductos(List<ProductoDemo> productos) {
        List<Producto> nuevos = new ArrayList<>(productos.size());
        Map<String, Integer> correlativos = new HashMap<>();
        for (ProductoDemo producto : productos) {
            Categoria categoria = categoriaRepository.findById(producto.categoriaId())
                    .orElseThrow(() -> new IllegalStateException("Categoría demo no encontrada"));
            Proveedor proveedor = proveedorRepository.findById(producto.proveedorId())
                    .orElseThrow(() -> new IllegalStateException("Proveedor demo no encontrado"));

            String prefijo = CodigoGenerator.prefijo(categoria.getNombre());
            int correlativo = correlativos.merge(prefijo, 1, Integer::sum);
            Unidad unidadBase = unidadRepository.findByCodigoIgnoreCase(producto.unidadBaseCodigo())
                    .orElseThrow(() -> new IllegalStateException(
                            "Unidad base demo no encontrada: " + producto.unidadBaseCodigo()));
            Producto entidad = new Producto(
                    CodigoGenerator.generar(prefijo, correlativo),
                    producto.nombre(),
                    unidadBase,
                    producto.stockMinimo(),
                    categoria,
                    proveedor);
            if (producto.unidadPresentacionCodigo() != null) {
                Unidad presentacion = unidadRepository.findByCodigoIgnoreCase(producto.unidadPresentacionCodigo())
                        .orElseThrow(() -> new IllegalStateException(
                                "Unidad de presentación demo no encontrada: " + producto.unidadPresentacionCodigo()));
                entidad.setUnidadPresentacion(presentacion);
                entidad.setFactorPresentacion(producto.factorPresentacion());
            }
            entidad.setDescripcion(producto.descripcion());
            entidad.setCostoUltimaCompra(producto.costoUltimaCompra());
            nuevos.add(entidad);
        }

        List<Producto> guardados = productoRepository.saveAll(nuevos);
        return guardados.stream()
                .map(p -> new ProductoCreado(p.getId(), p.getStockMinimo(), p.getCostoUltimaCompra(),
                        p.getProveedor().getId()))
                .toList();
    }

    @Override
    @Transactional
    public void aplicarEstado(List<EstadoProductoDemo> estados) {
        for (EstadoProductoDemo estado : estados) {
            Producto producto = productoRepository.findByIdForUpdate(estado.id())
                    .orElseThrow(() -> new IllegalStateException("Producto demo no encontrado"));
            producto.setStockActual(estado.stockActual());
            producto.setStockMinimo(estado.stockMinimo());
            producto.setActivo(estado.activo());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hayProductos() {
        return productoRepository.count() > 0;
    }

    private String nombreCategoriaUnico(String base) {
        String nombre = base;
        int sufijo = 1;
        while (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            nombre = base + " " + sufijo++;
        }
        return nombre;
    }
}
