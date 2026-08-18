package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// A "sajat profilom" (GET /api/users/me) teljes valasza - EZ tartalmazza az email cimet is,
// mert csak a bejelentkezett felhasznalo kaphatja meg sajat magarol (mas felhasznalokrol a
// szukebb UserSummaryResponse jar, lasd ott). A passwordHash termeszetesen itt sincs benne.
public record UserResponse(
        UUID id,
        String name,
        String email,
        String profilePictureUrl,
        String city,
        Double reliabilityScore,
        Instant createdAt,
        // Az osszes sportprofil beagyazva, hogy egy kerdesbol megkapja a frontend a teljes
        // "Profilom" oldalhoz szukseges adatot (lasd ProfilePage.jsx).
        List<UserSportProfileResponse> sportProfiles
) {

    public static UserResponse from(User user) {
        // A User.sportProfiles JPA lista minden elemet vegigalakitjuk a sajat DTO-jukka
        // (lasd UserSportProfileResponse.from) - .stream()...toList(): funkcionalis
        // stilusu, egysoros lista-atalakitas explicit for-ciklus helyett.
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
