package com.botlobby.backend.entity;

import com.botlobby.backend.entity.enums.LobbyStatus;
import com.botlobby.backend.entity.enums.SkillLevel;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lobbies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Celvaros neve, szabad szoveg - NEM GPS-koordinata alapu szures.
    @Column(nullable = false)
    private String city;

    // Naiv, idozona nelkuli helyi ido. Ha kesobb tobb idozonat/orszagot kell kezelni, OffsetDateTime-ra alakithato.
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(name = "min_skill_level", length = 20)
    private SkillLevel minSkillLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LobbyStatus status = LobbyStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // A jelenlegi letszamot (csak ACCEPTED tagok) NEM itt taroljuk, hanem a service reteg szamolja
    // menet kozben a LobbyParticipantRepository-n keresztul, hogy elkeruljuk a duplikalt/inkonzisztens allapotot.
    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LobbyParticipant> participants = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = LobbyStatus.OPEN;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lobby other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
