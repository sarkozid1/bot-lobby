package com.botlobby.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

// A "sports" tabla lekepezese - ez egy egyszeru, csaknem statikus referencia-tabla
// (foci, tenisz, kosarlabda, stb.), amit a V8 migracio tolt fel kezdeti adatokkal
// (lasd db/migration/V8__seed_sports.sql), es a SportController csak olvassa (nincs
// letrehozo/torlo vegpont hozza - uj sportot egyelore csak adatbazis-migracioval lehet felvenni).
@Entity
@Table(name = "sports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // A sport neve (pl. "Foci"), unique = true miatt nem lehet ket egyforma nevu sport.
    @Column(nullable = false, unique = true)
    private String name;

    // Opcionalis ikon-kep URL-je a frontend szamara (jelenleg nincs hasznalva - a frontend
    // helyette a sport nevenek elso betujebol general egy kis kor-ikont, lasd LobbyCard.jsx).
    @Column(name = "icon_url")
    private String iconUrl;

    // Lasd User.java - ugyanaz az id-alapu equals/hashCode minta minden entitasnal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sport other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
