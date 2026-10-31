package com.taller.reporte.controller;

import com.taller.reporte.dto.response.ProveedorBajoStock;
import com.taller.reporte.service.ReporteBajoStockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteBajoStockController {

    private final ReporteBajoStockService reporteBajoStockService;

    @GetMapping("/bajo-stock")
    public List<ProveedorBajoStock> bajoStock() {
        return reporteBajoStockService.consultar();
    }
}