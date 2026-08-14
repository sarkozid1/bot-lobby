package com.botlobby.backend.dto.user;

import com.botlobby.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SportProfileRequest(
        @NotNull(message = "A sport megadasa kotelezo.")
        UUID sportId,

        @NotNull(message = "A skillLevel megadasa kotelezo.")
        SkillLevel skillLevel
) {
}
