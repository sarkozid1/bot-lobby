package com.botlobby.backend.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

// POST /api/lobbies/{lobbyId}/ratings kerelem teste - a lobby id-je mar az URL-ben van,
// itt csak azt kell megadni, hogy KIT ertekelunk es hogyan (lasd RatingModal.jsx a frontenden).
public record RatingRequest(
        @NotNull(message = "Az ertekelt felhasznalo megadasa kotelezo.")
        UUID ratedUserId,

        @NotNull(message = "A csillagok szama kotelezo.")
        @Min(value = 1, message = "Legalabb 1 csillag.")
        @Max(value = 5, message = "Legfeljebb 5 csillag.")
        Integer stars,

        // List<@Size(max = 50) String>: a @Size annotacio itt NEM a listara vonatkozik
        // (nem a listaelemek szamat korlatozza), hanem a lista MINDEN EGYES elemenek
        // (String) a hosszara - ez a Bean Validation "container element constraint" formaja.
        List<@Size(max = 50) String> tags,

        @Size(max = 2000)
        String comment,

        // Explicit kotelezo mezo (nem csak alapertelmezetten false) - a kliensnek tudatosan
        // el kell dontenie, hogy megjelent-e a masik fel az esemenyen.
        @NotNull(message = "A noShow megadasa kotelezo.")
        Boolean noShow
) {
}
