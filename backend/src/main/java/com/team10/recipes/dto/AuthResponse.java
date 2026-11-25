package com.team10.recipes.dto;

public record AuthResponse(
        Long id,
        String username,
        String email,
        String role
) {
}