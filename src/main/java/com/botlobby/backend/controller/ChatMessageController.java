package com.botlobby.backend.controller;

import com.botlobby.backend.dto.chat.ChatMessageResponse;
import com.botlobby.backend.security.UserPrincipal;
import com.botlobby.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lobbies/{id}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping
    public List<ChatMessageResponse> getHistory(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable UUID id) {
        return chatMessageService.getHistory(id, principal.getId());
    }
}
