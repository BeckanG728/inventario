package com.taller.monitoreo.service;

import com.taller.monitoreo.dto.response.AlertaStock;

import java.util.List;

public interface AlertaService {

    String MENSAJE_SIN_ALERTAS = "No hay productos con bajo stock";

    List<AlertaStock> generarAlertas();

    String resumir(List<AlertaStock> alertas);
}
