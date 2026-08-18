package com.botlobby.backend.entity;

import com.botlobby.backend.entity.enums.SkillLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// Egy felhasznalo egy sporthoz tartozo skill-szintjet tarolja - ez egy tobb-a-tobbhoz
// kapcsolo tabla (User <-> Sport) EXTRA adattal (a skillLevel), ezert nem sima @ManyToMany,
// hanem egy sajat entitas ket @ManyToOne kapcsolattal.
@Entity
@Table(
        // Az uniqueConstraints biztositja, hogy egy felhasznalonak egy adott sporthoz csak
        // EGY profilja lehessen (ne lehessen ketszer felvenni "foci"-t kulonbozo skill-lel).
        // Erre epit a UserService.upsertSportProfile "insert vagy update" logikaja is.
        name = "user_sport_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_sport_profiles_user_sport", columnNames = {"user_id", "sport_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSportProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // A profil tulajdonosa. FetchType.LAZY: a User objektum csak akkor tolt be az
    // adatbazisbol, ha ténylegesen hozzaferunk (pl. userSportProfile.getUser().getName()) -
    // igy egy sportprofil-lista lekerdezese nem huz be feleslegesen teljes User rekordokat.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Melyik sportrol van szo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    // EnumType.STRING: az adatbazisban olvashato szoveg kerul tarolasra (pl. "BEGINNER"),
    // NEM a Java enum sorszama (ordinal) - igy ha kesobb uj erteket veszunk fel az enum
    // kozepere, a mar mentett sorok jelentese nem valtozik meg veletlenul.
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 20)
    private SkillLevel skillLevel;

    // Lasd User.java - ugyanaz az id-alapu equals/hashCode minta minden entitasnal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSportProfile other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
