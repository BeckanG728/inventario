package com.taller.reporte.service.impl;

import com.taller.alerta.service.AlertaService;
import com.taller.reporte.dto.response.ProveedorBajoStock;
import com.taller.reporte.service.BajoStockAgrupador;
import com.taller.reporte.service.ReporteBajoStockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporteBajoStockServiceImpl implements ReporteBajoStockService {

    private final AlertaService alertaService;

    @Override
    public List<ProveedorBajoStock> consultar() {
        return BajoStockAgrupador.agrupar(alertaService.generarAlertas());
    }
}