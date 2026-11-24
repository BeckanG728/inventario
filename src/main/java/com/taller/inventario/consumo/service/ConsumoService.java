package com.taller.inventario.consumo.service;

import com.taller.inventario.consumo.dto.request.ConsumoRequest;
import com.taller.inventario.consumo.dto.response.ConsumoResponse;
import com.taller.inventario.consumo.dto.response.ProductoFrecuenteResponse;
import java.util.List;

public interface ConsumoService {

    ConsumoResponse registrar(ConsumoRequest request, Long usuarioId);

    /** Los 5 productos activos más consumidos en los últimos 90 días, para el formulario de consumo. */
    List<ProductoFrecuenteResponse> productosFrecuentes();
}
