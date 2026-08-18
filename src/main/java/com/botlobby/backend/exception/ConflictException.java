package com.botlobby.backend.exception;

// Sajat kivetel-tipus HTTP 409 Conflict-hoz (lasd GlobalExceptionHandler.handleConflict).
// Pl. amikor regisztracioskor mar foglalt az email cim, vagy valaki masodszor probal
// jelentkezni ugyanarra a lobbyra.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
