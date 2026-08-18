package com.botlobby.backend.repository;

import com.botlobby.backend.entity.UserSportProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Lasd UserRepository.java a "derived query" minta magyarazataert.
public interface UserSportProfileRepository extends JpaRepository<UserSportProfile, UUID> {

    // Egy felhasznalo osszes sportprofilja (pl. profil-oldalon a "Sportprofiljaim" lista).
    List<UserSportProfile> findByUserId(UUID userId);

    // Egy adott felhasznalo egy adott sporthoz tartozo profilja - ebbol dol el a
    // UserService.upsertSportProfile-ban, hogy uj sort kell-e beszurni, vagy egy
    // meglevo skillLevel-t kell frissiteni (lasd az entitas uniqueConstraints-jet is).
    Optional<UserSportProfile> findByUserIdAndSportId(UUID userId, UUID sportId);

    boolean existsByUserIdAndSportId(UUID userId, UUID sportId);
}
