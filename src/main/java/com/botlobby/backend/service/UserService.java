package com.botlobby.backend.service;

import com.botlobby.backend.dto.user.SportProfileRequest;
import com.botlobby.backend.dto.user.SportProfileUpsertResult;
import com.botlobby.backend.dto.user.UserResponse;
import com.botlobby.backend.dto.user.UserSportProfileResponse;
import com.botlobby.backend.dto.user.UserSummaryResponse;
import com.botlobby.backend.dto.user.UserUpdateRequest;
import com.botlobby.backend.entity.Sport;
import com.botlobby.backend.entity.User;
import com.botlobby.backend.entity.UserSportProfile;
import com.botlobby.backend.exception.ResourceNotFoundException;
import com.botlobby.backend.repository.SportRepository;
import com.botlobby.backend.repository.UserRepository;
import com.botlobby.backend.repository.UserSportProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SportRepository sportRepository;
    private final UserSportProfileRepository userSportProfileRepository;

    @Transactional(readOnly = true)
    public UserResponse getById(UUID userId) {
        return UserResponse.from(findUserOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getPublicProfile(UUID userId) {
        return UserSummaryResponse.from(findUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UserUpdateRequest request) {
        User user = findUserOrThrow(userId);
        user.setName(request.name());
        user.setCity(request.city());
        user.setProfilePictureUrl(request.profilePictureUrl());
        return UserResponse.from(user);
    }

    @Transactional
    public SportProfileUpsertResult upsertSportProfile(UUID userId, SportProfileRequest request) {
        User user = findUserOrThrow(userId);
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen sport."));

        return userSportProfileRepository.findByUserIdAndSportId(userId, request.sportId())
                .map(existing -> {
                    existing.setSkillLevel(request.skillLevel());
                    return new SportProfileUpsertResult(UserSportProfileResponse.from(existing), false);
                })
                .orElseGet(() -> {
                    UserSportProfile profile = UserSportProfile.builder()
                            .user(user)
                            .sport(sport)
                            .skillLevel(request.skillLevel())
                            .build();
                    UserSportProfile saved = userSportProfileRepository.save(profile);
                    return new SportProfileUpsertResult(UserSportProfileResponse.from(saved), true);
                });
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen felhasznalo."));
    }
}
