package com.taller.consumo.service;

import com.taller.auth.entity.Usuario;
import com.taller.consumo.domain.ConsumoItem;
import com.taller.movimiento.entity.MovimientoInventario;

import java.util.List;

public interface ConsumoStockService {

    /**
     * RN207/US-12: valida todos los ítems y descuenta el stock de forma atómica.
     * Si un ítem no existe, está inactivo, tiene cantidad inválida o no hay stock
     * suficiente, no se aplica ningún descuento ni se registra ningún movimiento.
     */
    List<MovimientoInventario> validarYDescontarStock(List<ConsumoItem> items,
                                                      Usuario usuario,
                                                      Long consumoId,
                                                      String motivo);
}
