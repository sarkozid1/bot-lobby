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

// Ez a STOMP-os megfeleloje a REST @RestController-eknek - de itt NEM HTTP kerest, hanem
// egy STOMP uzenetet fogad. @Controller (nem @RestController), mert ez nem HTTP-valaszt ad
// vissza kozvetlenul, hanem tovabbi STOMP uzeneteket kuld (lasd messagingTemplate.convertAndSend).
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    // Kezi eszkoz STOMP uzenetek kuldesehez BARMELY cimre, barmikor - nem csak valaszkent
    // egy bejovo uzenetre (szemben azzal, ahogy egy REST controller csak a sajat HTTP
    // valaszat tudja visszakuldeni).
    private final SimpMessagingTemplate messagingTemplate;

    // @MessageMapping("/lobbies/{lobbyId}/chat.send"): a WebSocketConfig-ban beallitott
    // "/app" elotaggal egyutt ez a teljes cim "/app/lobbies/{lobbyId}/chat.send" - ide kuld
    // uzenetet a frontend uj chatuzenet elkuldesekor (lasd api/ws.js sendChatMessage).
    // @DestinationVariable: a cim {lobbyId} reszet olvassa ki, hasonloan a REST @PathVariable-hoz.
    // @Payload: az uzenet JSON teste automatikusan ChatMessageRequest-te alakul.
    // Principal principal: a StompAuthChannelInterceptor altal a CONNECT-kor beallitott
    // hitelesitett felhasznalo (lasd StompAuthChannelInterceptor.authenticate).
    @MessageMapping("/lobbies/{lobbyId}/chat.send")
    public void sendMessage(@DestinationVariable UUID lobbyId,
                             @Payload ChatMessageRequest request,
                             Principal principal) {
        UUID senderId = extractUserId(principal);
        // Ugyanaz a ChatMessageService, amit a REST ChatMessageController is hasznal az
        // elozmeny-lekerdezeshez - a validacio/jogosultsag-ellenorzes/mentes egy helyen van.
        ChatMessageResponse response = chatMessageService.saveMessage(lobbyId, senderId, request.content());
        // Kezzel, expliciten kuldjuk szet a mentett uzenetet MINDEN feliratkozottnak a
        // "/topic/lobbies/{lobbyId}" cimre - ezt kapja meg elo uzenetkent minden nyitott
        // ChatPanel.jsx, ami eppen erre a lobbyra van feliratkozva (a kuldo sajat magat is
        // beleertve - igy nem kell kulon, kliens-oldali "optimista" allapotfrissitest irni).
        messagingTemplate.convertAndSend("/topic/lobbies/" + lobbyId, response);
    }

    // Ha a sendMessage(...) barmilyen kivetelt dob (pl. ForbiddenOperationException, mert
    // valaki nem-engedelyezett lobbyba probal irni), ez a metodus kapja el - HTTP-nel a
    // GlobalExceptionHandler jatssza ezt a szerepet, STOMP-nal ez a @MessageExceptionHandler.
    // @SendToUser("/queue/errors"): a hibauzenetet KIZAROLAG a hibat okozo kliensnek kuldi
    // vissza (nem broadcast-olja mindenkinek, mint a sikeres uzeneteket).
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

    // Lasd StompAuthChannelInterceptor.extractUserId - ugyanaz a minta: a Principal mogott
    // egy Spring Security Authentication all, amelynek principal-ja a sajat UserPrincipal-unk.
    private UUID extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new ForbiddenOperationException("Nincs hitelesitve a WS kapcsolat.");
    }
}
