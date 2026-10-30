package com.taller.movimiento.service;

import com.taller.auth.entity.Usuario;
import com.taller.movimiento.dto.request.AjusteRequest;
import com.taller.movimiento.dto.response.MovimientoResponse;

public interface MovimientoService {

    MovimientoResponse ajuste(AjusteRequest request, Usuario usuario);
}
