package com.botlobby.backend.dto.auth;

import java.util.UUID;

// Sikeres regisztracio/bejelentkezes valasza - ez tartalmazza a JWT access tokent, amit a
// frontend ezutan minden kesobbi kerelemben az "Authorization: Bearer <token>" fejlecben
// kuld vissza (lasd web-frontend/src/api/client.js, JwtAuthenticationFilter).
public record AuthResponse(
        String accessToken,
        // Mindig "Bearer" - a HTTP Authorization fejlec sema-neve, amit a kliens az
        // accessToken elé fuz ("Bearer <token>").
        String tokenType,
        UUID userId,
        String name,
        String email
) {
    // Kenyelmi factory metodus: mindig "Bearer" tipussal hozza letre a valaszt, hogy a
    // hivo helyeken (AuthService) ne kelljen ezt a szoveges konstanst megismetelni.
    public static AuthResponse bearer(String accessToken, UUID userId, String name, String email) {
        return new AuthResponse(accessToken, "Bearer", userId, name, email);
    }
}
