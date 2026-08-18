package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// POST /api/users/me/sport-profiles kerelem teste - egy sportot es a hozza tartozo
// skill-szintet ad meg. A vegpont "upsert": ha a felhasznalonak mar van profilja ehhez a
// sporthoz, csak frissiti a skillLevel-t; ha nincs, ujat hoz letre (lasd UserService,
// SportProfileUpsertResult).
public record SportProfileRequest(
        @NotNull(message = "A sport megadasa kotelezo.")
        UUID sportId,

        @NotNull(message = "A skillLevel megadasa kotelezo.")
        SkillLevel skillLevel
) {
}
