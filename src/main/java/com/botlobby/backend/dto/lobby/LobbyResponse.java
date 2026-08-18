package com.botlobby.backend.dto.lobby;

import com.botlobby.backend.dto.sport.SportResponse;
import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.enums.LobbyStatus;
import com.botlobby.backend.entity.enums.SkillLevel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

// Egy lobby teljes, publikus valasz-alakja - a LobbiesPage kartyai es a LobbyDetail nezet is
// ezt kapja. Beagyazza a kapcsolodo Sport es "szervezo" User DTO-jat is (nem csak az id-jukat),
// hogy a frontendnek ne kelljen kulon lekerdezest inditania a nevuk/ikonjuk megjelenitesehez.
public record LobbyResponse(
        UUID id,
        SportResponse sport,
        UserSummaryResponse creator,
        String title,
        String description,
        String city,
        LocalDateTime startTime,
        Integer maxParticipants,
        // NEM a Lobby entitas mezoje - ezt a service reteg szamolja ki menet kozben
        // (LobbyParticipantRepository.countByLobbyIdAndStatus ACCEPTED-re), lasd Lobby.java
        // megjegyzeset arrol, miert nem tarolodik ez kozvetlenul az adatbazisban.
        long currentParticipants,
        SkillLevel minSkillLevel,
        LobbyStatus status,
        Instant createdAt
) {

    // A currentParticipants-ot KULON parameterkent kapja (nem magabol a Lobby entitasbol),
    // mert az entitasnak nincs ilyen mezoje - a hivo (LobbyService) mar kiszamolta es ide adja at.
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
