package com.botlobby.backend.repository;

import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LobbyParticipantRepository extends JpaRepository<LobbyParticipant, UUID> {

    Optional<LobbyParticipant> findByLobbyIdAndUserId(UUID lobbyId, UUID userId);

    boolean existsByLobbyIdAndUserId(UUID lobbyId, UUID userId);

    List<LobbyParticipant> findByLobbyId(UUID lobbyId);

    List<LobbyParticipant> findByUserId(UUID userId);

    long countByLobbyIdAndStatus(UUID lobbyId, ParticipantStatus status);
}
