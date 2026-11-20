package com.taller.auditoria.controller;

import com.taller.auditoria.dto.response.AuditoriaResponse;
import com.taller.auditoria.service.AuditoriaService;
import com.taller.common.dto.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<PaginaResponse<AuditoriaResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String resultado) {

        return ResponseEntity.ok(PaginaResponse.from(
                auditoriaService.listar(page, size, desde, hasta, actor, tipo, resultado)));
    }
}
