package com.taller.sec.dto;

import java.time.Instant;

public record RegisterResponse(Long id, String username, String email, String role, Instant createdAt) {
}