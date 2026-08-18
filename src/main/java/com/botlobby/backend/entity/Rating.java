package com.botlobby.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Egy esemeny utani ertekeles: valaki (rater) pontoz valaki mast (ratedUser), egy adott
// lobbyhoz kotve. Ebbol szamolodik a User.reliabilityScore (lasd RatingService).
@Entity
@Table(
        name = "ratings",
        // Ugyanaz a szemely ugyanazt a masik szemelyt ugyanahhoz a lobbyhoz csak egyszer
        // ertekelheti - ezt a harom-oszlopos egyedi kulcs kenyszeriti ki.
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ratings_lobby_rater_rated_user",
                columnNames = {"lobby_id", "rater_id", "rated_user_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Melyik esemenyhez kapcsolodik az ertekeles.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lobby_id", nullable = false)
    private Lobby lobby;

    // Ki adta az ertekelest.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    // Kit ertekelnek.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_user_id", nullable = false)
    private User ratedUser;

    // 1-5 kozotti csillagszam (a validaciot a RatingRequest DTO @Min/@Max annotacioi vegzik,
    // mielott ez az entitas egyaltalan letrejonne).
    @Column(nullable = false)
    private Integer stars;

    // Szabadon valaszthato cimkek (pl. "pontos", "baratsagos") - kulon tablaban taroljuk
    // (@ElementCollection), mert egy ertekeleshez tobb cimke is tartozhat, es ezek csak
    // egyszeru String-ek, nem sajat entitasok.
    @ElementCollection
    @CollectionTable(name = "rating_tags", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "tag", nullable = false, length = 50)
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String comment;

    // true eseten a service reteg (RatingService) csokkenti a ratedUser.reliabilityScore-jat.
    @Column(name = "no_show", nullable = false)
    @Builder.Default
    private Boolean noShow = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (noShow == null) {
            noShow = false;
        }
    }

    // Lasd User.java - ugyanaz az id-alapu equals/hashCode minta minden entitasnal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
