package com.botlobby.backend.dto.lobby;

import com.botlobby.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record LobbyCreateRequest(
        @NotNull(message = "A sport megadasa kotelezo.")
        UUID sportId,

        @NotBlank(message = "A cim megadasa kotelezo.")
        @Size(max = 255)
        String title,

        @Size(max = 5000)
        String description,

        @NotBlank(message = "A varos megadasa kotelezo.")
        @Size(max = 255)
        String city,

        @NotNull(message = "A kezdesi idopont megadasa kotelezo.")
        @Future(message = "A kezdesi idopontnak a jovoben kell lennie.")
        LocalDateTime startTime,

        @NotNull(message = "A maxParticipants megadasa kotelezo.")
        @Positive(message = "A maxParticipants-nak pozitivnak kell lennie.")
        Integer maxParticipants,

        SkillLevel minSkillLevel
) {
}
