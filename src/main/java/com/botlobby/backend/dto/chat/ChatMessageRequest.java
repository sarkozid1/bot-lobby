package com.botlobby.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
        @NotBlank(message = "Az uzenet nem lehet ures.")
        @Size(max = 2000, message = "Az uzenet legfeljebb 2000 karakter lehet.")
        String content
) {
}
