package com.botlobby.backend.controller;

import com.botlobby.backend.dto.auth.AuthResponse;
import com.botlobby.backend.dto.auth.LoginRequest;
import com.botlobby.backend.dto.auth.RegisterRequest;
import com.botlobby.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// "Controller reteg": ez fogadja a nyers HTTP kereseket, es delegal a service retegnek -
// szandekosan NINCS itt semmi uzleti logika, csak a bejovo JSON DTO-ra alakitasa
// (@RequestBody) es a valasz visszaadasa. @RestController = @Controller + @ResponseBody:
// minden metodus visszateresi erteke automatikusan JSON-na alakul (nincs kulon nezet/template).
// @RequestMapping("/api/auth"): minden ide tartozo vegpont "/api/auth" alatt erhetok el.
// Lasd SecurityConfig-ban: ez az egyetlen vegpont-csoport, ami hitelesites NELKUL is elerheto
// (permitAll a "/api/auth/**" mintara) - logikus, hiszen meg nincs is JWT-je annak, aki
// regisztral/bejelentkezik.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // @Valid: a RegisterRequest mezoin levo Bean Validation annotaciok (@NotBlank, @Email, stb.)
    // itt futnak le, MIELOTT a metodus torzse egyaltalan elindulna - sikertelen validaciokor
    // a GlobalExceptionHandler.handleValidation kapja el a hibat.
    // @ResponseStatus(HttpStatus.CREATED): uj eroforras (felhasznalo) jott letre, ezert 201-et
    // adunk vissza a szokasos 200 helyett (REST konvencio).
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // Sikeres bejelentkezes -> 200 OK (alapertelmezett, nincs kulon @ResponseStatus).
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
