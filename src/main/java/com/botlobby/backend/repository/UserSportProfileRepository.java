package com.botlobby.backend.repository;

import com.botlobby.backend.entity.UserSportProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSportProfileRepository extends JpaRepository<UserSportProfile, UUID> {

    List<UserSportProfile> findByUserId(UUID userId);

    Optional<UserSportProfile> findByUserIdAndSportId(UUID userId, UUID sportId);

    boolean existsByUserIdAndSportId(UUID userId, UUID sportId);
}
