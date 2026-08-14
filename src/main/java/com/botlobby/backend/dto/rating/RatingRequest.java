package com.botlobby.backend.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record RatingRequest(
        @NotNull(message = "Az ertekelt felhasznalo megadasa kotelezo.")
        UUID ratedUserId,

        @NotNull(message = "A csillagok szama kotelezo.")
        @Min(value = 1, message = "Legalabb 1 csillag.")
        @Max(value = 5, message = "Legfeljebb 5 csillag.")
        Integer stars,

        List<@Size(max = 50) String> tags,

        @Size(max = 2000)
        String comment,

        @NotNull(message = "A noShow megadasa kotelezo.")
        Boolean noShow
) {
}
