package com.botlobby.backend.dto.user;

/**
 * created=true eseten a POST /api/users/me/sport-profiles 201-et ad vissza, kulonben 200-at
 * (mivel a vegpont upsert: mar letezo sportprofilnal csak a skillLevel-t frissiti).
 */
public record SportProfileUpsertResult(UserSportProfileResponse profile, boolean created) {
}
