package com.botlobby.backend.dto.lobby;

import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.enums.ParticipantStatus;

import java.time.Instant;
import java.util.UUID;

public record LobbyParticipantResponse(
        UUID id,
        UserSummaryResponse user,
        ParticipantStatus status,
        Instant joinedAt
) {

    public static LobbyParticipantResponse from(LobbyParticipant participant) {
        return new LobbyParticipantResponse(
                participant.getId(),
                UserSummaryResponse.from(participant.getUser()),
                participant.getStatus(),
                participant.getJoinedAt()
        );
    }
}
