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

// Lasd AuthController.java a controller-minta magyarazataert.
// EZ csak az uzenet-ELOZMENY REST-en keresztuli lekerdezeset vegzi (uj kapcsolodaskor a
// ChatPanel.jsx ezt hivja, hogy visszatoltse a korabbi beszelgetest) - a valos idejo
// kuldes/fogadas kulon, WebSocket/STOMP csatornan zajlik (lasd websocket/ChatWebSocketController).
@RestController
@RequestMapping("/api/lobbies/{id}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    // @AuthenticationPrincipal UserPrincipal principal: Spring Security automatikusan ide
    // fecskendezi a JwtAuthenticationFilter altal korabban beallitott bejelentkezett
    // felhasznalot (lasd SecurityContextHolder a JwtAuthenticationFilter-ben) - igy a
    // controllernek nem kell kezzel kibontania a JWT-t vagy az Authorization fejlecet.
    @GetMapping
    public List<ChatMessageResponse> getHistory(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable UUID id) {
        return chatMessageService.getHistory(id, principal.getId());
    }
}
