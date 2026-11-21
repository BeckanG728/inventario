package com.taller.catalogo.api.impl;

import com.taller.catalogo.api.PreparacionCatalogo;
import com.taller.catalogo.categoria.entity.Categoria;
import com.taller.catalogo.categoria.repository.CategoriaRepository;
import com.taller.catalogo.producto.entity.Producto;
import com.taller.catalogo.producto.repository.ProductoRepository;
import com.taller.catalogo.proveedor.entity.Proveedor;
import com.taller.catalogo.proveedor.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PreparacionCatalogoImpl implements PreparacionCatalogo {

    private static final String PREFIJO_SKU = "CP-";

    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public List<Long> crearCategorias(List<CategoriaDemo> categorias) {
        List<Long> ids = new ArrayList<>(categorias.size());
        for (CategoriaDemo categoria : categorias) {
            ids.add(categoriaRepository.save(
                    new Categoria(nombreCategoriaUnico(categoria.nombre()), categoria.descripcion())).getId());
        }
        return ids;
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
        for (ProductoDemo producto : productos) {
            Categoria categoria = categoriaRepository.findById(producto.categoriaId())
                    .orElseThrow(() -> new IllegalStateException("Categoría demo no encontrada"));
            Proveedor proveedor = proveedorRepository.findById(producto.proveedorId())
                    .orElseThrow(() -> new IllegalStateException("Proveedor demo no encontrado"));

            Producto entidad = new Producto(
                    producto.codigo(),
                    producto.nombre(),
                    producto.unidadMedida(),
                    producto.stockMinimo(),
                    categoria,
                    proveedor);
            entidad.setDescripcion(producto.descripcion());
            entidad.setCostoUltimaCompra(producto.costoUltimaCompra());
            nuevos.add(entidad);
        }

        List<Producto> guardados = productoRepository.saveAll(nuevos);
        for (Producto producto : guardados) {
            producto.setSku(PREFIJO_SKU + String.format(Locale.ROOT, "%06d", producto.getId()));
        }
        return guardados.stream()
                .map(p -> new ProductoCreado(p.getId(), p.getStockMinimo(), p.getCostoUltimaCompra(),
                        p.getProveedor().getId()))
                .toList();
    }

    @Override
    @Transactional
    public void aplicarEstado(List<EstadoProductoDemo> estados) {
        for (EstadoProductoDemo estado : estados) {
            Producto producto = productoRepository.findById(estado.id())
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
