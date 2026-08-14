package com.botlobby.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Az email cim megadasa kotelezo.")
        @Email(message = "Ervenytelen email cim.")
        String email,

        @NotBlank(message = "A jelszo megadasa kotelezo.")
        String password
) {
}
