package com.botlobby.backend.repository;

import com.botlobby.backend.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Lasd UserRepository.java a "derived query" minta magyarazataert.
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // Egy lobby osszes uzenete idorendben (regi -> uj) - uj WebSocket-kapcsolodaskor
    // ezzel toltodik vissza a korabbi beszelgetes elozmenye (lasd ChatMessageService,
    // ChatPanel.jsx a frontenden).
    List<ChatMessage> findByLobbyIdOrderBySentAtAsc(UUID lobbyId);
}
