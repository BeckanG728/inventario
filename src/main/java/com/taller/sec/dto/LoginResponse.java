package com.taller.sec.dto;

public record LoginResponse(String token, String tokenType, long expiresIn, Long userId, String role) {
}