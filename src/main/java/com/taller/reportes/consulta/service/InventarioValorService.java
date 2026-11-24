package com.taller.reportes.consulta.service;

import com.taller.reportes.dto.response.ValorInventarioPaginaResponse;
import com.taller.reportes.dto.response.ValorInventarioResponse;

public interface InventarioValorService {

    ValorInventarioResponse consultar();

    /** Versión paginada para la interfaz; los totales siguen siendo globales. */
    ValorInventarioPaginaResponse consultarPagina(int page, int size);
}