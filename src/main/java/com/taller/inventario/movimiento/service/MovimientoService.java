package com.taller.inventario.movimiento.service;

import com.taller.inventario.movimiento.dto.request.AjusteRequest;
import com.taller.inventario.movimiento.dto.request.EntradaRequest;
import com.taller.inventario.movimiento.dto.response.EntradaResponse;
import com.taller.inventario.movimiento.dto.response.MovimientoResponse;

public interface MovimientoService {

    MovimientoResponse ajuste(AjusteRequest request, Long usuarioId);

    EntradaResponse entrada(EntradaRequest request, Long usuarioId);
}
