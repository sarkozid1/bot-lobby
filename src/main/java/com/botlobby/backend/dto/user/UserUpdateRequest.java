package com.botlobby.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// A PUT /api/users/me kerelem teste (profil-szerkesztes, lasd ProfilePage.jsx "Adatok
// szerkesztese" urlapja). Szandekosan NEM tartalmaz email/jelszo mezot - azok kulon,
// dedikalt vegponton/folyamaton kereszul valtoztathatok (biztonsagi okokbol nem kevered a
// "nev/varos frissitese" es a "hitelesitesi adatok cseje" logikait egy vegpontban).
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
