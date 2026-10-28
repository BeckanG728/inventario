package com.taller.auth.dto.response;

import java.time.Instant;

public record RegisterResponse(Long id, String username, String role, Instant createdAt) {
}