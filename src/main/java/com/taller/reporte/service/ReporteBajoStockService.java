package com.taller.reporte.service;

import com.taller.reporte.dto.response.ProveedorBajoStock;
import java.util.List;

public interface ReporteBajoStockService {

    List<ProveedorBajoStock> consultar();
}