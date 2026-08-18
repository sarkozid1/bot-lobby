package com.botlobby.backend.exception;

// Sajat kivetel-tipus HTTP 403 Forbidden-hoz (lasd GlobalExceptionHandler.handleForbidden).
// Ezt akkor dobja a service reteg, amikor a felhasznalo hitelesitve van (tehat van ervenyes
// JWT-je), de nincs jogosultsaga a konkret muvelethez - pl. nem-szervezo probalja elfogadni
// egy masik felhasznalo jelentkezeset.
public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}
