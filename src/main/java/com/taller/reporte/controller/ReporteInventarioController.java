package com.taller.reporte.controller;

import com.taller.reporte.dto.response.ValorInventarioResponse;
import com.taller.reporte.service.InventarioValorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteInventarioController {

    private final InventarioValorService inventarioValorService;

    @GetMapping("/valor-inventario")
    public ValorInventarioResponse valorInventario() {
        return inventarioValorService.consultar();
    }
}