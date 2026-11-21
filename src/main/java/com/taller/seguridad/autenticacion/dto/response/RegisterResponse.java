package com.taller.seguridad.autenticacion.dto.response;

import java.time.Instant;

public record RegisterResponse(Long id, String username, String role, Instant createdAt) {
}