package com.botlobby.backend.dto.chat;

import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.ChatMessage;

import java.time.Instant;
import java.util.UUID;

// Egy chatuzenet kimeno alakja - EZT kapja meg a kliens mind a REST-en (GET /api/lobbies/{id}/messages,
// elozmeny-betolteskor), mind a WebSocket-en (uj, elo uzenet erkezesekor a /topic/lobbies/{id}
// feliratkozason keresztul) - igy a frontend (ChatPanel.jsx) ugyanazzal a kettel tudja kezelni mindket forrast.
public record ChatMessageResponse(
        UUID id,
        UUID lobbyId,
        UserSummaryResponse sender,
        String content,
        Instant sentAt
) {

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getLobby().getId(),
                UserSummaryResponse.from(message.getSender()),
                message.getContent(),
                message.getSentAt()
        );
    }
}
