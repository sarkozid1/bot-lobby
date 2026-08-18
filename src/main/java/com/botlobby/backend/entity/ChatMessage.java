package com.botlobby.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

// Egy lobbyhoz tartozo chatuzenet. A valos idejo kezbesites WebSocket/STOMP-on tortenik
// (lasd websocket/ChatWebSocketController), de minden uzenet ide is mentodik, hogy uj
// kapcsolodaskor vissza lehessen tolteni a korabbi beszelgetest (lasd ChatMessageService).
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // Melyik lobby chat-jehez tartozik.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lobby_id", nullable = false)
    private Lobby lobby;

    // Ki kuldte az uzenetet.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Az uzenet szovege - columnDefinition = "TEXT": nincs karakterszam-korlat.
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    @PrePersist
    void onCreate() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
    }

    // Lasd User.java - ugyanaz az id-alapu equals/hashCode minta minden entitasnal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
