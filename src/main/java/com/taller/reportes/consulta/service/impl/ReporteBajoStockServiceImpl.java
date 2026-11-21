package com.taller.reportes.consulta.service.impl;

import com.taller.controlstock.alerta.service.AlertaService;
import com.taller.reportes.dto.response.ProveedorBajoStock;
import com.taller.reportes.consulta.service.BajoStockAgrupador;
import com.taller.reportes.consulta.service.ReporteBajoStockService;
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