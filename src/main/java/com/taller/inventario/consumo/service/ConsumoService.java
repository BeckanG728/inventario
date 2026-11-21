package com.taller.inventario.consumo.service;

import com.taller.inventario.consumo.dto.request.ConsumoRequest;
import com.taller.inventario.consumo.dto.response.ConsumoResponse;

public interface ConsumoService {

    ConsumoResponse registrar(ConsumoRequest request, Long usuarioId);
}
