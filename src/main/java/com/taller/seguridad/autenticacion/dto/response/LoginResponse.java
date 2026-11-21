package com.taller.seguridad.autenticacion.dto.response;

public record LoginResponse(String token, String tokenType, long expiresIn, Long userId, String role) {
}