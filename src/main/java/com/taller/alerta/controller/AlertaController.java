package com.taller.alerta.controller;

import com.taller.alerta.dto.response.AlertaStock;
import com.taller.alerta.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaStock>> listar() {
        return ResponseEntity.ok(alertaService.generarAlertas());
    }
}
