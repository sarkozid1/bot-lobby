package com.botlobby.backend.dto.chat;

import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.ChatMessage;

import java.time.Instant;
import java.util.UUID;

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
