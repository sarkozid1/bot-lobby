package com.botlobby.backend.repository;

import com.botlobby.backend.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Lasd UserRepository.java a "derived query" minta magyarazataert.
public interface SportRepository extends JpaRepository<Sport, UUID> {

    // "SELECT * FROM sports WHERE name = ?" - jelenleg nincs kozvetlenul hasznalva a service retegben,
    // de kesz eszkozkent elerheto, ha nev alapjan kellene sportot keresni.
    Optional<Sport> findByName(String name);

    // "SELECT COUNT(*) > 0 FROM sports WHERE name = ?".
    boolean existsByName(String name);
}
