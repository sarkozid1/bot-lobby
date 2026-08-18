package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.UserSportProfile;
import com.botlobby.backend.entity.enums.SkillLevel;

import java.util.UUID;

// Lasd SportResponse.java a from(...) mapper-minta magyarazataert.
// Egy felhasznalo egy sporthoz tartozo skill-profilja - a sport nevet is kilapitva (sportName)
// adja vissza, hogy a frontendnek ne kelljen kulon lekerdezest inditania csak a nev miatt.
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
