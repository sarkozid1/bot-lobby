package com.botlobby.backend.service;

import com.botlobby.backend.dto.rating.RatingRequest;
import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.LobbyParticipant;
import com.botlobby.backend.entity.Rating;
import com.botlobby.backend.entity.Sport;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.enums.LobbyStatus;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import com.botlobby.backend.exception.ConflictException;
import com.botlobby.backend.repository.LobbyParticipantRepository;
import com.botlobby.backend.repository.LobbyRepository;
import com.botlobby.backend.repository.RatingRepository;
import com.botlobby.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LobbyParticipantRepository lobbyParticipantRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    private User creator;
    private User participantUser;
    private Lobby pastLobby;

    @BeforeEach
    void setUp() {
        creator = User.builder().id(UUID.randomUUID()).name("Szervezo").email("szervezo@example.com").reliabilityScore(5.0).build();
        participantUser = User.builder().id(UUID.randomUUID()).name("Reszvevo").email("reszvevo@example.com").reliabilityScore(5.0).build();
        Sport sport = Sport.builder().id(UUID.randomUUID()).name("Foci").build();

        pastLobby = Lobby.builder()
                .id(UUID.randomUUID())
                .sport(sport)
                .creator(creator)
                .title("Focizas")
                .city("Budapest")
                .startTime(LocalDateTime.now().minusDays(1))
                .maxParticipants(5)
                .status(LobbyStatus.CLOSED)
                .build();
    }

    private LobbyParticipant acceptedParticipant() {
        return LobbyParticipant.builder()
                .id(UUID.randomUUID()).lobby(pastLobby).user(participantUser).status(ParticipantStatus.ACCEPTED).build();
    }

    @Test
    void rate_whenFirstRating_scoreEqualsSubmittedStars() {
        RatingRequest request = new RatingRequest(participantUser.getId(), 4, List.of(), null, false);

        when(lobbyRepository.findById(pastLobby.getId())).thenReturn(Optional.of(pastLobby));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantUser.getId())).thenReturn(Optional.of(participantUser));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), creator.getId()))
                .thenReturn(Optional.empty());
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), participantUser.getId()))
                .thenReturn(Optional.of(acceptedParticipant()));
        when(ratingRepository.existsByLobbyIdAndRaterIdAndRatedUserId(pastLobby.getId(), creator.getId(), participantUser.getId()))
                .thenReturn(false);
        when(ratingRepository.countByRatedUserId(participantUser.getId())).thenReturn(0L);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        ratingService.rate(pastLobby.getId(), creator.getId(), request);

        assertThat(participantUser.getReliabilityScore()).isEqualTo(4.0);
    }

    @Test
    void rate_whenNoShowTrue_treatsAsZeroStars() {
        RatingRequest request = new RatingRequest(participantUser.getId(), 5, List.of(), null, true);

        when(lobbyRepository.findById(pastLobby.getId())).thenReturn(Optional.of(pastLobby));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantUser.getId())).thenReturn(Optional.of(participantUser));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), creator.getId()))
                .thenReturn(Optional.empty());
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), participantUser.getId()))
                .thenReturn(Optional.of(acceptedParticipant()));
        when(ratingRepository.existsByLobbyIdAndRaterIdAndRatedUserId(pastLobby.getId(), creator.getId(), participantUser.getId()))
                .thenReturn(false);
        when(ratingRepository.countByRatedUserId(participantUser.getId())).thenReturn(0L);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        ratingService.rate(pastLobby.getId(), creator.getId(), request);

        assertThat(participantUser.getReliabilityScore()).isEqualTo(0.0);
    }

    @Test
    void rate_whenExistingRatingsPresent_computesWeightedAverage() {
        participantUser.setReliabilityScore(5.0);
        RatingRequest request = new RatingRequest(participantUser.getId(), 3, List.of(), null, false);

        when(lobbyRepository.findById(pastLobby.getId())).thenReturn(Optional.of(pastLobby));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantUser.getId())).thenReturn(Optional.of(participantUser));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), creator.getId()))
                .thenReturn(Optional.empty());
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), participantUser.getId()))
                .thenReturn(Optional.of(acceptedParticipant()));
        when(ratingRepository.existsByLobbyIdAndRaterIdAndRatedUserId(pastLobby.getId(), creator.getId(), participantUser.getId()))
                .thenReturn(false);
        when(ratingRepository.countByRatedUserId(participantUser.getId())).thenReturn(1L);
        when(ratingRepository.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        ratingService.rate(pastLobby.getId(), creator.getId(), request);

        assertThat(participantUser.getReliabilityScore()).isEqualTo(4.0); // (5*1 + 3) / 2 = 4.0
    }

    @Test
    void rate_whenSelfRating_throwsConflict() {
        RatingRequest request = new RatingRequest(creator.getId(), 5, List.of(), null, false);

        assertThatThrownBy(() -> ratingService.rate(pastLobby.getId(), creator.getId(), request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rate_whenLobbyNotYetHappened_throwsConflict() {
        pastLobby.setStartTime(LocalDateTime.now().plusDays(1));
        RatingRequest request = new RatingRequest(participantUser.getId(), 5, List.of(), null, false);

        when(lobbyRepository.findById(pastLobby.getId())).thenReturn(Optional.of(pastLobby));
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(userRepository.findById(participantUser.getId())).thenReturn(Optional.of(participantUser));

        assertThatThrownBy(() -> ratingService.rate(pastLobby.getId(), creator.getId(), request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rate_whenRaterWasNotInLobby_throwsConflict() {
        User outsider = User.builder().id(UUID.randomUUID()).name("Kivulallo").email("kivulallo@example.com").reliabilityScore(5.0).build();
        RatingRequest request = new RatingRequest(participantUser.getId(), 5, List.of(), null, false);

        when(lobbyRepository.findById(pastLobby.getId())).thenReturn(Optional.of(pastLobby));
        when(userRepository.findById(outsider.getId())).thenReturn(Optional.of(outsider));
        when(userRepository.findById(participantUser.getId())).thenReturn(Optional.of(participantUser));
        when(lobbyParticipantRepository.findByLobbyIdAndUserId(pastLobby.getId(), outsider.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ratingService.rate(pastLobby.getId(), outsider.getId(), request))
                .isInstanceOf(ConflictException.class);
    }
}
