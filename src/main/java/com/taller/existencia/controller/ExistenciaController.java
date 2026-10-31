package com.taller.existencia.controller;

import com.taller.existencia.dto.response.ExistenciaResponse;
import com.taller.existencia.service.ExistenciaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/existencias")
@RequiredArgsConstructor
public class ExistenciaController {

    private final ExistenciaService existenciaService;

    @GetMapping
    public List<ExistenciaResponse> consultar(@RequestParam(required = false) List<Long> ids) {
        return existenciaService.consultar(ids);
    }
}