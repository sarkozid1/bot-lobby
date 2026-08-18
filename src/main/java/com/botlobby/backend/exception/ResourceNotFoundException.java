package com.botlobby.backend.exception;

// Sajat, "jelolo" kivetel-tipus: barhol a kodban eldobva, a GlobalExceptionHandler
// automatikusan HTTP 404 Not Found valaszra alakitja (lasd handleNotFound). Pl. amikor
// a LobbyService egy nem letezo lobby-id-t kap.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
