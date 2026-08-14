package com.botlobby.backend.dto.lobby;

import com.botlobby.backend.dto.sport.SportResponse;
import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.enums.LobbyStatus;
import com.botlobby.backend.entity.enums.SkillLevel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record LobbyResponse(
        UUID id,
        SportResponse sport,
        UserSummaryResponse creator,
        String title,
        String description,
        String city,
        LocalDateTime startTime,
        Integer maxParticipants,
        long currentParticipants,
        SkillLevel minSkillLevel,
        LobbyStatus status,
        Instant createdAt
) {

    public static LobbyResponse from(Lobby lobby, long currentParticipants) {
        return new LobbyResponse(
                lobby.getId(),
                SportResponse.from(lobby.getSport()),
                UserSummaryResponse.from(lobby.getCreator()),
                lobby.getTitle(),
                lobby.getDescription(),
                lobby.getCity(),
                lobby.getStartTime(),
                lobby.getMaxParticipants(),
                currentParticipants,
                lobby.getMinSkillLevel(),
                lobby.getStatus(),
                lobby.getCreatedAt()
        );
    }
}
