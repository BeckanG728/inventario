package com.taller.monitoreo.dto.response;

import com.taller.catalogo.producto.dto.response.ProductoResponse;
import com.taller.compartido.EstadoStock;
import java.math.BigDecimal;

/** Vista operativa de existencias: datos del producto más clasificación y sugerencia de reposición. */
public record ExistenciaConsultaResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String unidadMedida,
        Integer stockActual,
        Integer stockMinimo,
        boolean stockCritico,
        BigDecimal costoUltimaCompra,
        boolean activo,
        Long categoriaId,
        String categoriaNombre,
        Long proveedorId,
        String proveedorRazonSocial,
        EstadoStock estadoStock,
        Integer cantidadSugerida
) {

    public static ExistenciaConsultaResponse desde(ProductoResponse base, EstadoStock estadoStock,
                                                   Integer cantidadSugerida) {
        return new ExistenciaConsultaResponse(
                base.id(), base.codigo(), base.nombre(), base.descripcion(),
                base.unidadMedida(), base.stockActual(), base.stockMinimo(), base.stockCritico(),
                base.costoUltimaCompra(), base.activo(), base.categoriaId(), base.categoriaNombre(),
                base.proveedorId(), base.proveedorRazonSocial(), estadoStock, cantidadSugerida);
    }
}
