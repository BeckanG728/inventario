package com.taller.controlstock.alerta.service;

import com.taller.controlstock.alerta.dto.response.AlertaStock;

import java.util.List;

public interface AlertaService {

    String MENSAJE_SIN_ALERTAS = "No hay productos con bajo stock";

    List<AlertaStock> generarAlertas();

    String resumir(List<AlertaStock> alertas);
}
