package com.taller.reporte.service;

import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import java.time.LocalDate;

public interface ReporteMovimientosService {

    ReporteMovimientosResponse consultar(LocalDate desde, LocalDate hasta, TipoMovimiento tipo);
}
