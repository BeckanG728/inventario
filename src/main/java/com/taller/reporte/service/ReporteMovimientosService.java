package com.taller.reporte.service;

import com.taller.movimiento.enums.TipoMovimiento;
import com.taller.reporte.dto.response.ReporteMovimientosResponse;
import com.taller.reporte.dto.response.ReporteMovimientosPaginaResponse;
import java.time.LocalDate;

public interface ReporteMovimientosService {

    /** Todos los movimientos del filtro, sin paginar (usado por la exportación). */
    ReporteMovimientosResponse consultar(LocalDate desde, LocalDate hasta, TipoMovimiento tipo);

    /** Una página de movimientos del filtro; los totales por tipo cubren todo el filtro. */
    ReporteMovimientosPaginaResponse consultarPagina(LocalDate desde, LocalDate hasta, TipoMovimiento tipo,
                                                     int page, int size);
}
