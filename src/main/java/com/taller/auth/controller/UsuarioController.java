package com.taller.auth.controller;

import com.taller.auth.dto.request.ActualizarUsuarioRequest;
import com.taller.auth.dto.request.ResetPasswordRequest;
import com.taller.auth.dto.response.UsuarioResponse;
import com.taller.auth.service.UsuarioService;
import com.taller.common.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<PaginaResponse<UsuarioResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(PaginaResponse.from(usuarioService.listar(page, size, username, role, activo)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<UsuarioResponse> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(usuarioService.resetPassword(id, request));
    }
}
