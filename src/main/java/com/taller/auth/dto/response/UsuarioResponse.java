package com.taller.auth.dto.response;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String username,
        String role,
        boolean activo,
        Instant createdAt
) {
}
