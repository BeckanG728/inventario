package com.taller.monitoreo.dto.request;

import com.taller.compartido.EstadoStock;

/** Filtros de la vista operativa de existencias. Los productos inactivos nunca se incluyen. */
public record ExistenciaConsultaFiltros(
        int page,
        int size,
        String nombre,
        Long categoriaId,
        Long proveedorId,
        EstadoStock estadoStock
) {
}
