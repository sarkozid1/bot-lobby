package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String profilePictureUrl,
        String city,
        Double reliabilityScore,
        Instant createdAt,
        List<UserSportProfileResponse> sportProfiles
) {

    public static UserResponse from(User user) {
        List<UserSportProfileResponse> profiles = user.getSportProfiles().stream()
                .map(UserSportProfileResponse::from)
                .toList();

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                user.getCity(),
                user.getReliabilityScore(),
                user.getCreatedAt(),
                profiles
        );
    }
}
