package com.botlobby.backend.dto.user;

/**
 * created=true eseten a POST /api/users/me/sport-profiles 201-et ad vissza, kulonben 200-at
 * (mivel a vegpont upsert: mar letezo sportprofilnal csak a skillLevel-t frissiti).
 * Ez a record NEM kozvetlenul a HTTP valasz teste - a UserController olvassa ki belole a
 * "created" jelzot a helyes HTTP status kivalasztasahoz, es csak a "profile" mezot kuldi
 * vissza JSON-kent (lasd UserController.upsertSportProfile).
 */
public record SportProfileUpsertResult(UserSportProfileResponse profile, boolean created) {
}
