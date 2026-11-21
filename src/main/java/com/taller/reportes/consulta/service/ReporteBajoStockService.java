package com.taller.reportes.consulta.service;

import com.taller.reportes.dto.response.ProveedorBajoStock;
import java.util.List;

public interface ReporteBajoStockService {

    List<ProveedorBajoStock> consultar();
}