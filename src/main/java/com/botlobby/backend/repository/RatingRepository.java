package com.botlobby.backend.repository;

import com.botlobby.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Lasd UserRepository.java a "derived query" minta magyarazataert.
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // Egy felhasznalo osszes kapott ertekelese - ez jelenik meg a profiloldalon (UserProfilePage.jsx),
    // es ebbol szamolodik ujra a reliabilityScore is (lasd RatingService).
    List<Rating> findByRatedUserId(UUID ratedUserId);

    // Egy adott lobbyhoz tartozo osszes ertekeles.
    List<Rating> findByLobbyId(UUID lobbyId);

    // Egy adott ertekelo-ertekelt-lobby harmas mar letezo ertekelese, ha van - ebbol dol el,
    // hogy valaki ujra probal-e ertekelni (lasd RatingService, es az entitas uniqueConstraints-jet).
    Optional<Rating> findByLobbyIdAndRaterIdAndRatedUserId(UUID lobbyId, UUID raterId, UUID ratedUserId);

    boolean existsByLobbyIdAndRaterIdAndRatedUserId(UUID lobbyId, UUID raterId, UUID ratedUserId);

    // Hany ertekelest kapott osszesen egy felhasznalo - a profiloldalon a "(N)" szamlalohoz.
    long countByRatedUserId(UUID ratedUserId);
}
