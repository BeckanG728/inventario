package com.taller.monitoreo.service.impl;

import com.taller.catalogo.producto.entity.Producto;
import com.taller.catalogo.producto.mapper.ProductoMapper;
import com.taller.catalogo.producto.repository.ProductoRepository;
import com.taller.catalogo.producto.specification.ProductoSpecification;
import com.taller.compartido.EstadoStock;
import com.taller.monitoreo.dto.request.ExistenciaConsultaFiltros;
import com.taller.monitoreo.dto.response.ExistenciaConsultaResponse;
import com.taller.monitoreo.service.ExistenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExistenciaServiceImpl implements ExistenciaService {

    private static final int TAMANO_PAGINA_MAXIMO = 100;

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    /** Listado operativo: exclusivamente productos activos, con filtros y paginación. */
    @Override
    @Transactional(readOnly = true)
    public Page<ExistenciaConsultaResponse> listarActivos(ExistenciaConsultaFiltros filtros) {
        Pageable pageable = PageRequest.of(
                Math.max(filtros.page(), 0),
                Math.clamp(filtros.size(), 1, TAMANO_PAGINA_MAXIMO),
                Sort.by("nombre", "id"));

        Specification<Producto> consulta = ProductoSpecification.activoIgual(true);
        if (filtros.nombre() != null && !filtros.nombre().isBlank()) {
            String nombre = filtros.nombre().trim();
            consulta = consulta.and(ProductoSpecification.nombreContiene(nombre)
                    .or(ProductoSpecification.codigoContiene(nombre)));
        }
        if (filtros.categoriaId() != null) {
            consulta = consulta.and(ProductoSpecification.categoriaId(filtros.categoriaId()));
        }
        if (filtros.proveedorId() != null) {
            consulta = consulta.and(ProductoSpecification.proveedorId(filtros.proveedorId()));
        }
        if (filtros.estadoStock() != null) {
            consulta = consulta.and(ProductoSpecification.estadoStock(filtros.estadoStock()));
        }

        return productoRepository.findAll(consulta, pageable)
                .map(producto -> {
                    EstadoStock estado = EstadoStock.de(producto.getStockActual(), producto.getStockMinimo());
                    return ExistenciaConsultaResponse.desde(
                            productoMapper.toResponse(producto),
                            estado,
                            estado.cantidadSugerida(producto.getStockActual(), producto.getStockMinimo()));
                });
    }
}
