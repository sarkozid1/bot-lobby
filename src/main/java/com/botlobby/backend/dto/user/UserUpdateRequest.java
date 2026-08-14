package com.botlobby.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "A nev megadasa kotelezo.")
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String city,

        @Size(max = 1024)
        String profilePictureUrl
) {
}
