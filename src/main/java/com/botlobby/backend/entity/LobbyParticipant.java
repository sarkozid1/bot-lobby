package com.botlobby.backend.entity;

import com.botlobby.backend.entity.enums.ParticipantStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Osszekoti egy User-t egy Lobby-val: ez a "jelentkezes" rekordja, a state-jevel (status)
// egyutt vegigkoveti a teljes eletciklusat PENDING (jelentkezett) -> ACCEPTED/DECLINED
// (a szervezo dontott) -> LEFT (a reszvevo kesobb kilepett).
@Entity
@Table(
        // Egy felhasznalo egy lobbyhoz csak egyszer jelentkezhet - ezt az adatbazis kenyszeriti ki,
        // nem csak a Java-kod (lasd LobbyService.join, ami emiatt is ellenorzi elore a duplikaciot).
        name = "lobby_participants",
        uniqueConstraints = @UniqueConstraint(name = "uq_lobby_participants_lobby_user", columnNames = {"lobby_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobbyParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Melyik lobbyhoz tartozik ez a jelentkezes.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lobby_id", nullable = false)
    private Lobby lobby;

    // Ki jelentkezett.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // A jelentkezes aktualis allapota - uj jelentkezes mindig PENDING-kent indul,
    // lasd ParticipantStatus.java az osszes lehetseges ertekert.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.PENDING;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
        if (status == null) {
            status = ParticipantStatus.PENDING;
        }
    }

    // Lasd User.java - ugyanaz az id-alapu equals/hashCode minta minden entitasnal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LobbyParticipant other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
