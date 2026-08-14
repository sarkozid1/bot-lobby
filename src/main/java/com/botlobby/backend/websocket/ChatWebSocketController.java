package com.botlobby.backend.websocket;

import com.botlobby.backend.dto.chat.ChatMessageRequest;
import com.botlobby.backend.dto.chat.ChatMessageResponse;
import com.botlobby.backend.exception.ForbiddenOperationException;
import com.botlobby.backend.security.UserPrincipal;
import com.botlobby.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/lobbies/{lobbyId}/chat.send")
    public void sendMessage(@DestinationVariable UUID lobbyId,
                             @Payload ChatMessageRequest request,
                             Principal principal) {
        UUID senderId = extractUserId(principal);
        ChatMessageResponse response = chatMessageService.saveMessage(lobbyId, senderId, request.content());
        messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyId, response);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new ForbiddenOperationException("Nincs hitelesitve a WS kapcsolat.");
    }
}
