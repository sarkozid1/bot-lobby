package com.botlobby.backend.repository;

import com.botlobby.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Spring Data JPA "derived query" repository: nincs itt semmilyen implementacio, csak egy
// interface. Spring induláskor legenerál egy proxy-osztalyt, ami a metodusnevekbol
// (findBy.../existsBy...) automatikusan SQL-t allit elo. A JpaRepository<User, UUID> orokles
// mar magaban hozza a szokasos CRUD metodusokat is (save, findById, findAll, deleteById, stb.),
// ezeket itt nem kell ujra kiirni.
public interface UserRepository extends JpaRepository<User, UUID> {

    // "SELECT * FROM users WHERE email = ?" - bejelentkezeskor es email-alapu keresesnel hasznalt
    // (lasd CustomUserDetailsService, AuthService.login). Optional, mert lehet, hogy nincs ilyen email.
    Optional<User> findByEmail(String email);

    // "SELECT COUNT(*) > 0 FROM users WHERE email = ?" - regisztracioskor ellenorzi, hogy foglalt-e
    // mar az email cim (lasd AuthService.register), gyorsabb mint egy teljes findByEmail(...).isPresent().
    boolean existsByEmail(String email);
}
