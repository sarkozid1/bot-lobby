package com.botlobby.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO (Data Transfer Object): ez a JSON test alakja, amit a kliens POST /api/auth/register-re
// kuld. SOSEM az entitast (User) hasznaljuk kozvetlenul a kerelem/valasz alakjakent - igy a
// belso adatbazis-struktura (pl. passwordHash mezo neve) es a publikus API fuggetlen tud maradni
// egymastol, es csak azok a mezok kerulnek be, amikre ténylegesen szukseg van bejovo adatkent.
//
// A mezokre irt Bean Validation annotaciok (@NotBlank, @Email, @Size) automatikusan lefutnak,
// mielott a controller metodusa egyaltalan meghivodna - ehhez a controller parameteren
// @Valid kell (lasd AuthController.register), es sikertelen validaciokor a
// GlobalExceptionHandler.handleValidation fogja el a hibat 400-as valasszal.
public record RegisterRequest(
        @NotBlank(message = "A nev megadasa kotelezo.")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Az email cim megadasa kotelezo.")
        @Email(message = "Ervenytelen email cim.")
        @Size(max = 255)
        String email,

        // Csak a nyers, meg NEM hash-elt jelszo utazik ide a kliensrol - a hash-eleset
        // az AuthService vegzi (PasswordEncoder.encode), mielott barmi mentodne.
        @NotBlank(message = "A jelszo megadasa kotelezo.")
        @Size(min = 8, max = 100, message = "A jelszonak legalabb 8 karakter hosszunak kell lennie.")
        String password,

        // Opcionalis mezok - nincs rajtuk @NotBlank, csak @Size a tulzottan hosszu ertekek ellen.
        @Size(max = 255)
        String city,

        @Size(max = 1024)
        String profilePictureUrl
) {
}
