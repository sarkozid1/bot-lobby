package com.botlobby.backend.dto.auth;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String name,
        String email
) {
    public static AuthResponse bearer(String accessToken, UUID userId, String name, String email) {
        return new AuthResponse(accessToken, "Bearer", userId, name, email);
    }
}
