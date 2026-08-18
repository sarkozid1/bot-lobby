package com.botlobby.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

// Egysegesen felepitett hiba-valasz test, amit MINDEN hibaz vegpont ugyanigy ad vissza
// (lasd GlobalExceptionHandler) - a frontend (ErrorBanner.jsx) erre a formara szamit.
// "record": Java-s, veglegesen immutable adat-hordozo osztaly - a mezokbol automatikusan
// generalodik konstruktor, getterek (pl. timestamp() metodus a timestamp mezohoz) es equals/
// hashCode/toString, kevesebb bojlerplate kod kell hozza mint egy sima osztalyhoz.
@JsonInclude(JsonInclude.Include.NON_NULL) // a null mezok (pl. fieldErrors, ha nincs validacios hiba) kimaradnak a JSON-bol
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        // Mezo-szintu validacios hibak terkepe (mezonev -> hibauzenet), csak validacios
        // hibaknal (lasd GlobalExceptionHandler.handleValidation) toltodik ki.
        Map<String, String> fieldErrors
) {

    // Kenyelmi "factory" metodus egyszeru (nem-validacios) hibakhoz - a timestamp-et
    // automatikusan az aktualis idopontra allitja, fieldErrors nelkul.
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    // Ugyanaz, de validacios hibaknal, ahol mezonkenti hibauzeneteket is kuldunk.
    public static ApiError of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors);
    }
}
