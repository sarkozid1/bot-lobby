package com.botlobby.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "A nev megadasa kotelezo.")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Az email cim megadasa kotelezo.")
        @Email(message = "Ervenytelen email cim.")
        @Size(max = 255)
        String email,

        @NotBlank(message = "A jelszo megadasa kotelezo.")
        @Size(min = 8, max = 100, message = "A jelszonak legalabb 8 karakter hosszunak kell lennie.")
        String password,

        @Size(max = 255)
        String city,

        @Size(max = 1024)
        String profilePictureUrl
) {
}
