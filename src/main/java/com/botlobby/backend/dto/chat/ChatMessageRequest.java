package com.botlobby.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Egy elkuldott chatuzenet payload-ja. Ezt NEM egy hagyomanyos REST vegpont fogadja, hanem
// a STOMP WebSocket csatorna (/app/lobbies/{lobbyId}/chat.send, lasd ChatWebSocketController)
// - a kuldo es a lobby azonositasa a WebSocket-kapcsolat kontextusabol (JWT + URL) tortenik,
// nem ebbol a testbol.
public record ChatMessageRequest(
        @NotBlank(message = "Az uzenet nem lehet ures.")
        @Size(max = 2000, message = "Az uzenet legfeljebb 2000 karakter lehet.")
        String content
) {
}
