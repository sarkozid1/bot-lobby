package com.botlobby.backend.dto.lobby;

import com.botlobby.backend.entity.enums.SkillLevel;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

// POST /api/lobbies kerelem teste (lasd CreateLobbyPage/LobbiesPage "Uj lobby" urlapja
// a frontenden). A "creator" mezo szandekosan HIANYZIK innen - azt a szerver a bejelentkezett
// felhasznalobol (JWT-bol) allapitja meg, sosem a kliens altal kuldott adatbol (lasd LobbyController,
// LobbyService.create), kulonben barki masnak a neveben hozhatna letre lobbyt.
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

        // @Future: a Bean Validation ellenorzi, hogy az idopont a jovoben legyen -
        // multbeli idoponttal nem lehet lobbyt letrehozni.
        @NotNull(message = "A kezdesi idopont megadasa kotelezo.")
        @Future(message = "A kezdesi idopontnak a jovoben kell lennie.")
        LocalDateTime startTime,

        @NotNull(message = "A maxParticipants megadasa kotelezo.")
        @Positive(message = "A maxParticipants-nak pozitivnak kell lennie.")
        Integer maxParticipants,

        // Opcionalis - null eseten nincs skill-megkotes a csatlakozashoz.
        SkillLevel minSkillLevel
) {
}
