package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.UserSportProfile;
import com.botlobby.backend.entity.enums.SkillLevel;

import java.util.UUID;

public record UserSportProfileResponse(UUID id, UUID sportId, String sportName, SkillLevel skillLevel) {

    public static UserSportProfileResponse from(UserSportProfile profile) {
        return new UserSportProfileResponse(
                profile.getId(),
                profile.getSport().getId(),
                profile.getSport().getName(),
                profile.getSkillLevel()
        );
    }
}
