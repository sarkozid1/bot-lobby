package com.botlobby.backend.repository;

import com.botlobby.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    List<Rating> findByRatedUserId(UUID ratedUserId);

    List<Rating> findByLobbyId(UUID lobbyId);

    Optional<Rating> findByLobbyIdAndRaterIdAndRatedUserId(UUID lobbyId, UUID raterId, UUID ratedUserId);

    boolean existsByLobbyIdAndRaterIdAndRatedUserId(UUID lobbyId, UUID raterId, UUID ratedUserId);

    long countByRatedUserId(UUID ratedUserId);
}
