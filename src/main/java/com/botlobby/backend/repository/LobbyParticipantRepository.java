package com.botlobby.backend.repository;

import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Lasd UserRepository.java a "derived query" minta magyarazataert.
public interface LobbyParticipantRepository extends JpaRepository<LobbyParticipant, UUID> {

    // Egy adott felhasznalo egy adott lobbyhoz tartozo jelentkezese - ez adja meg, hogy
    // "mar jelentkezett-e" es milyen statusszal (lasd LobbyService.join/accept/decline).
    Optional<LobbyParticipant> findByLobbyIdAndUserId(UUID lobbyId, UUID userId);

    // Gyors "mar letezik-e jelentkezes" ellenorzes, entitas betoltese nelkul.
    boolean existsByLobbyIdAndUserId(UUID lobbyId, UUID userId);

    // Egy lobby osszes jelentkezoje/reszvevoje, allapottol fuggetlenul (lasd LobbyController
    // reszvevo-lista vegpontja - a lathatosagot a service reteg szuri tovabb szervezo/elfogadott alapjan).
    List<LobbyParticipant> findByLobbyId(UUID lobbyId);

    // Egy felhasznalo osszes jelentkezese (jelenleg nincs kozvetlenul hasznalva egyetlen
    // vegpont mogott sem, de keszen all pl. egy "sajat jelentkezeseim" nezethez).
    List<LobbyParticipant> findByUserId(UUID userId);

    // "SELECT COUNT(*) FROM lobby_participants WHERE lobby_id = ? AND status = ?" - ebbol szamolja
    // a LobbyService a lobby AKTUALIS letszamat (mindig csak az ACCEPTED statuszuakat szamolva),
    // es dol el, hogy elerte-e a maxParticipants-ot (ekkor a lobby FULL-ra valt).
    long countByLobbyIdAndStatus(UUID lobbyId, ParticipantStatus status);
}
