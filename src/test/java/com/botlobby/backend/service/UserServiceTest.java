package com.botlobby.backend.service;

import com.botlobby.backend.dto.user.SportProfileRequest;
import com.botlobby.backend.dto.user.SportProfileUpsertResult;
import com.botlobby.backend.entity.Sport;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.UserSportProfile;
import com.botlobby.backend.entity.enums.SkillLevel;
import com.botlobby.backend.repository.SportRepository;
import com.botlobby.backend.repository.UserRepository;
import com.botlobby.backend.repository.UserSportProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SportRepository sportRepository;

    @Mock
    private UserSportProfileRepository userSportProfileRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Sport sport;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).name("Teszt Elek").email("teszt@example.com").build();
        sport = Sport.builder().id(UUID.randomUUID()).name("Foci").build();
    }

    @Test
    void upsertSportProfile_whenNoExistingProfile_createsNew() {
        SportProfileRequest request = new SportProfileRequest(sport.getId(), SkillLevel.INTERMEDIATE);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(sportRepository.findById(sport.getId())).thenReturn(Optional.of(sport));
        when(userSportProfileRepository.findByUserIdAndSportId(user.getId(), sport.getId())).thenReturn(Optional.empty());
        when(userSportProfileRepository.save(any(UserSportProfile.class))).thenAnswer(inv -> {
            UserSportProfile profile = inv.getArgument(0);
            profile.setId(UUID.randomUUID());
            return profile;
        });

        SportProfileUpsertResult result = userService.upsertSportProfile(user.getId(), request);

        assertThat(result.created()).isTrue();
        assertThat(result.profile().skillLevel()).isEqualTo(SkillLevel.INTERMEDIATE);
    }

    @Test
    void upsertSportProfile_whenExistingProfile_updatesSkillLevel() {
        SportProfileRequest request = new SportProfileRequest(sport.getId(), SkillLevel.PRO);
        UserSportProfile existing = UserSportProfile.builder()
                .id(UUID.randomUUID()).user(user).sport(sport).skillLevel(SkillLevel.BEGINNER).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(sportRepository.findById(sport.getId())).thenReturn(Optional.of(sport));
        when(userSportProfileRepository.findByUserIdAndSportId(user.getId(), sport.getId())).thenReturn(Optional.of(existing));

        SportProfileUpsertResult result = userService.upsertSportProfile(user.getId(), request);

        assertThat(result.created()).isFalse();
        assertThat(existing.getSkillLevel()).isEqualTo(SkillLevel.PRO);
        verify(userSportProfileRepository, never()).save(any());
    }
}
