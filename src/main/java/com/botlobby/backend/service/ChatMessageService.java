package com.botlobby.backend.service;

import com.botlobby.backend.dto.chat.ChatMessageResponse;
import com.botlobby.backend.entity.ChatMessage;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.exception.ForbiddenOperationException;
import com.botlobby.backend.exception.ResourceNotFoundException;
import com.botlobby.backend.repository.ChatMessageRepository;
import com.botlobby.backend.repository.LobbyParticipantRepository;
import com.botlobby.backend.repository.LobbyRepository;
import com.botlobby.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final int MAX_CONTENT_LENGTH = 2000;

    private final LobbyRepository lobbyRepository;
    private final UserRepository userRepository;
    private final LobbyParticipantRepository lobbyParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessageResponse saveMessage(UUID lobbyId, UUID senderId, String content) {
        if (content == null || content.isBlank()) {
            throw new ConflictException("Az uzenet nem lehet ures.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ConflictException("Az uzenet legfeljebb " + MAX_CONTENT_LENGTH + " karakter lehet.");
        }

        Lobby lobby = findLobbyOrThrow(lobbyId);
        User sender = findUserOrThrow(senderId);
        requireChatAccess(lobby, senderId);

        ChatMessage message = ChatMessage.builder()
                .lobby(lobby)
                .sender(sender)
                .content(content)
                .build();

        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(UUID lobbyId, UUID requesterId) {
        Lobby lobby = findLobbyOrThrow(lobbyId);
        requireChatAccess(lobby, requesterId);

        return chatMessageRepository.findByLobbyIdOrderBySentAtAsc(lobbyId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    // Csak a szervezo vagy elfogadott (ACCEPTED) resztvevok chatelhetnek/olvashatjak a lobby chatjet -
    // a meg fuggoben levo (PENDING) jelentkezoknek nincs hozzaferese, amig a szervezo el nem fogadja oket.
    private void requireChatAccess(Lobby lobby, UUID userId) {
        boolean isCreator = lobby.getCreator().getId().equals(userId);
        boolean isAcceptedParticipant = lobbyParticipantRepository.findByLobbyIdAndUserId(lobby.getId(), userId)
                .map(participant -> participant.getStatus() == ParticipantStatus.ACCEPTED)
                .orElse(false);

        if (!isCreator && !isAcceptedParticipant) {
            throw new ForbiddenOperationException("Csak a lobby szervezoje vagy elfogadott resztvevoi chatelhetnek.");
        }
    }

    private Lobby findLobbyOrThrow(UUID lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen lobby."));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen felhasznalo."));
    }
}
