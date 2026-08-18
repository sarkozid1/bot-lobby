package com.botlobby.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice: ez az osztaly NEM egy konkret vegpont, hanem egy globalis
// "elkapo halo" MINDEN @RestController felett - ha egy controller/service metodusban
// barhol eldobodik egy itt kezelt kivetel-tipus, Spring automatikusan ide iranyitja,
// es a metodus visszaterese lesz a tenyleges HTTP valasz (nem kell minden controller
// vegpontban try/catch-et irni).
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Sajat kivetelunk -> 404 Not Found.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // Sajat kivetelunk -> 409 Conflict.
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // Sajat kivetelunk -> 403 Forbidden.
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenOperationException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    // Spring Security sajat kivetele, amikor egy hitelesitett felhasznalonak nincs
    // jogosultsaga egy vedett eroforrashoz (pl. @PreAuthorize / manualis ellenorzes buktatja el).
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Nincs jogosultsagod ehhez a muvelethez.", request);
    }

    // Rossz email/jelszo bejelentkezeskor (lasd AuthService.login -> authenticationManager.authenticate).
    // Szandekosan altalanos uzenetet ad vissza ("Hibas email cim vagy jelszo"), nem arulja el
    // kulon, hogy az email nem letezik-e vagy csak a jelszo rossz - ez tamadas elleni jo gyakorlat.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Hibas email cim vagy jelszo.", request);
    }

    // Minden mas hitelesitesi hiba (pl. hianyzo/lejart JWT) altalanos elkapoja.
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Sikertelen hitelesites.", request);
    }

    // A DTO-kon levo Bean Validation annotaciok (pl. @NotBlank, @Email, @Min) sikertelen
    // ellenorzesekor Spring ezt a kivetelt dobja - itt szedjuk ki belole mezonkent a
    // hibauzeneteket, es rakjuk bele az ApiError.fieldErrors terkepebe (lasd ApiError.java).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Ervenytelen kerelem.",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Vegso biztonsagi halo: minden mas, elore nem latott kivetelt (pl. NullPointerException,
    // adatbazis-hiba) 500 Internal Server Error-ra alakit, es NEM szivarogtatja ki a technikai
    // reszleteket (stack trace-t) a kliens fele - csak egy altalanos uzenetet.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Varatlan hiba tortent.", request);
    }

    // Kozos segedmetodus: osszerakja az egyseges ApiError test-et a HTTP statusszal egyutt,
    // hogy a fenti kezelo metodusokban ne kelljen ezt minden alkalommal ismetelni.
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
