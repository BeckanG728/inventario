package com.taller.consumo.service;

import com.taller.auth.entity.Usuario;
import com.taller.consumo.dto.request.ConsumoRequest;
import com.taller.consumo.dto.response.ConsumoResponse;

public interface ConsumoService {

    ConsumoResponse registrar(ConsumoRequest request, Usuario usuario);
}
