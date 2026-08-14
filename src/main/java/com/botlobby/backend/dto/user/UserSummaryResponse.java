package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.User;

import java.util.UUID;

/**
 * Publikus, mas felhasznalok szamara is lathato kivonat egy Userrol
 * (nincs benne email/passwordHash - lobby creator/participant megjelenitesehez).
 */
public record UserSummaryResponse(UUID id, String name, String profilePictureUrl, Double reliabilityScore) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getProfilePictureUrl(), user.getReliabilityScore());
    }
}
