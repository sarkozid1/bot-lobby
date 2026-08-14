package com.botlobby.backend.repository;

import com.botlobby.backend.entity.Sport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SportRepository extends JpaRepository<Sport, UUID> {

    Optional<Sport> findByName(String name);

    boolean existsByName(String name);
}
