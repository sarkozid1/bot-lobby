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

    // A "sajat profilom" (GET /api/users/me) mogott - a teljes UserResponse-t adja vissza
    // (email-lel egyutt), mert a hivo mindig maga a bejelentkezett felhasznalo.
    @Transactional(readOnly = true)
    public UserResponse getById(UUID userId) {
        return UserResponse.from(findUserOrThrow(userId));
    }

    // Egy MASIK felhasznalo publikus profilja (GET /api/users/{id}) - szandekosan a szukebb
    // UserSummaryResponse-t adja vissza, nincs benne email cim.
    @Transactional(readOnly = true)
    public UserSummaryResponse getPublicProfile(UUID userId) {
        return UserSummaryResponse.from(findUserOrThrow(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UserUpdateRequest request) {
        User user = findUserOrThrow(userId);
        // JPA "dirty checking": a setterek meghivasa MAGABAN eleg ahhoz, hogy Hibernate a
        // tranzakcio vegen (commit-kor) UPDATE SQL-t generaljon - nem kell explicit
        // userRepository.save(user)-t hivni, mert a "user" mar egy managed (kovetett) entitas.
        user.setName(request.name());
        user.setCity(request.city());
        user.setProfilePictureUrl(request.profilePictureUrl());
        return UserResponse.from(user);
    }

    // "Upsert": vagy frissiti a mar letezo sportprofilt, vagy letrehoz egy ujat - a hivo
    // (UserController) a "created" flag alapjan dönti el, hogy 200-at vagy 201-et adjon vissza.
    @Transactional
    public SportProfileUpsertResult upsertSportProfile(UUID userId, SportProfileRequest request) {
        User user = findUserOrThrow(userId);
        Sport sport = sportRepository.findById(request.sportId())
                .orElseThrow(() -> new ResourceNotFoundException("Nincs ilyen sport."));

        // Optional.map(...).orElseGet(...): ha VAN mar profil ehhez a sporthoz, a map-ag fut
        // (csak frissites, created=false); ha NINCS, az orElseGet-ag fut (uj letrehozasa,
        // created=true). Ez a funkcionalis stilus valtja ki az if/else blokkot.
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
