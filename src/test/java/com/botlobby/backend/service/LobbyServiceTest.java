package com.botlobby.backend.service;

import com.botlobby.backend.dto.lobby.LobbyParticipantResponse;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.Sport;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.enums.LobbyStatus;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.exception.ForbiddenOperationException;
import com.botlobby.backend.repository.LobbyParticipantRepository;
import com.botlobby.backend.repository.LobbyRepository;
import com.botlobby.backend.repository.SportRepository;
import com.botlobby.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LobbyParticipantRepository lobbyParticipantRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private User creator;
    private User applicant;
    private Lobby lobby;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(UUID.randomUUID()).name("Szervezo").email("szervezo@example.com").build();
        applicant = User.builder().id(UUID.randomUUID()).name("Jelentkezo").email("jelentkezo@example.com").build();
        Sport sport = Sport.builder().id(UUID.randomUUID()).name("Foci").build();

        lobby = Lobby.builder()
                .id(UUID.randomUUID())
                .sport(sport)
                .creator(creator)
                .title("Focizas")
                .city("Budapest")
                .startTime(LocalDateTime.now().plusDays(1))
                .maxParticipants(1)
                .status(LobbyStatus.OPEN)
                .build();
    }

    @Test
    void join_whenValid_createsPendingParticipant() {
        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(userRepository.findById(applicant.getId())).thenReturn(Optional.of(applicant));
        when(lobbyParticipantRepository.existsByLobbyIdAndUserId(lobby.getId(), applicant.getId())).thenReturn(false);
        when(lobbyParticipantRepository.save(any(LobbyParticipant.class))).thenAnswer(inv -> inv.getArgument(0));

        LobbyParticipantResponse response = lobbyService.join(lobby.getId(), applicant.getId());

        assertThat(response.status()).isEqualTo(ParticipantStatus.PENDING);
        assertThat(response.user().id()).isEqualTo(applicant.getId());
    }

    @Test
    void join_whenCreatorTriesToJoinOwnLobby_throwsConflict() {
        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));

        assertThatThrownBy(() -> lobbyService.join(lobby.getId(), creator.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void join_whenAlreadyApplied_throwsConflict() {
        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(userRepository.findById(applicant.getId())).thenReturn(Optional.of(applicant));
        when(lobbyParticipantRepository.existsByLobbyIdAndUserId(lobby.getId(), applicant.getId())).thenReturn(true);

        assertThatThrownBy(() -> lobbyService.join(lobby.getId(), applicant.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void join_whenLobbyNotOpen_throwsConflict() {
        lobby.setStatus(LobbyStatus.FULL);

        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(userRepository.findById(applicant.getId())).thenReturn(Optional.of(applicant));

        assertThatThrownBy(() -> lobbyService.join(lobby.getId(), applicant.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void accept_whenCapacityReached_marksLobbyFull() {
        LobbyParticipant participant = LobbyParticipant.builder()
                .id(UUID.randomUUID()).lobby(lobby).user(applicant).status(ParticipantStatus.PENDING).build();

        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(lobby.getId(), applicant.getId()))
                .thenReturn(Optional.of(participant));
        when(lobbyParticipantRepository.countByLobbyIdAndStatus(lobby.getId(), ParticipantStatus.ACCEPTED))
                .thenReturn(0L);

        LobbyParticipantResponse response = lobbyService.accept(lobby.getId(), applicant.getId(), creator.getId());

        assertThat(response.status()).isEqualTo(ParticipantStatus.ACCEPTED);
        assertThat(lobby.getStatus()).isEqualTo(LobbyStatus.FULL);
    }

    @Test
    void accept_whenNotCreator_throwsForbidden() {
        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));

        assertThatThrownBy(() -> lobbyService.accept(lobby.getId(), applicant.getId(), applicant.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void accept_whenLobbyAlreadyFull_throwsConflict() {
        LobbyParticipant participant = LobbyParticipant.builder()
                .id(UUID.randomUUID()).lobby(lobby).user(applicant).status(ParticipantStatus.PENDING).build();

        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(lobby.getId(), applicant.getId()))
                .thenReturn(Optional.of(participant));
        when(lobbyParticipantRepository.countByLobbyIdAndStatus(lobby.getId(), ParticipantStatus.ACCEPTED))
                .thenReturn(1L);

        assertThatThrownBy(() -> lobbyService.accept(lobby.getId(), applicant.getId(), creator.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void leave_whenAcceptedParticipantLeavesFullLobby_reopensLobby() {
        lobby.setStatus(LobbyStatus.FULL);
        LobbyParticipant participant = LobbyParticipant.builder()
                .id(UUID.randomUUID()).lobby(lobby).user(applicant).status(ParticipantStatus.ACCEPTED).build();

        when(lobbyRepository.findById(lobby.getId())).thenReturn(Optional.of(lobby));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(lobby.getId(), applicant.getId()))
                .thenReturn(Optional.of(participant));
        when(lobbyParticipantRepository.countByLobbyIdAndStatus(lobby.getId(), ParticipantStatus.ACCEPTED))
                .thenReturn(0L);

        lobbyService.leave(lobby.getId(), applicant.getId());

        assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.LEFT);
        assertThat(lobby.getStatus()).isEqualTo(LobbyStatus.OPEN);
    }
}
