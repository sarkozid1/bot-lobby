package com.botlobby.backend.dto.lobby;

import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.enums.ParticipantStatus;

import java.time.Instant;
import java.util.UUID;

// Egy jelentkezes/reszvetel valasz-alakja a "Reszvevok" listahoz (lasd LobbyDetailContent.jsx).
// Kulon figyelem: a LobbyController a reszvevo-listat csak a szervezonek es az elfogadott
// tagoknak adja vissza (403-at kap barki mas) - ez a DTO onmagaban nem vegez jogosultsag-ellenorzest.
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
