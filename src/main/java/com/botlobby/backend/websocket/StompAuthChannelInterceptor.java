package com.botlobby.backend.websocket;

import com.botlobby.backend.entity.Lobby;
import com.botlobby.backend.entity.enums.ParticipantStatus;
import com.botlobby.backend.repository.LobbyParticipantRepository;
import com.botlobby.backend.repository.LobbyRepository;
import com.botlobby.backend.security.CustomUserDetailsService;
import com.botlobby.backend.security.JwtService;
import com.botlobby.backend.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ket dolgot vegez:
 * 1. CONNECT: a STOMP CONNECT frame "Authorization: Bearer <token>" native headerebol hitelesit -
 *    a SecurityConfig HTTP szinten permitAll-ozza a /ws/** handshake-et, az uzenet-szintu
 *    auth-ot itt vegezzuk. Sikeres hitelesites utan a StompHeaderAccessor-on beallitott
 *    user a tovabbi @MessageMapping metodusokban Principal-kent erheto el.
 * 2. SUBSCRIBE: a "/topic/lobbies/{id}" cimre valo feliratkozast is jogosultsaghoz kotjuk
 *    (csak a lobby szervezoje vagy elfogadott resztvevoi). CONNECT-kor hitelesitett, de
 *    a lobbyhoz nem tartozo (vagy onnan mar kilepett/elutasitott) felhasznalo enelkul
 *    barmelyik lobby chatjere fel tudott volna iratkozni es passzivan olvasni azt.
 *
 * A ChannelInterceptor a STOMP vilag megfeleloje a HTTP JwtAuthenticationFilter-nek: az
 * osszes bejovo uzenet athalad ezen (lasd WebSocketConfig.configureClientInboundChannel),
 * mielott celba erne barmelyik @MessageMapping metodusnal.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    // Regex, ami a "/topic/lobbies/{UUID}" alaku cimeket ismeri fel, es kicsomagolja belole
    // a lobby UUID-jet (a zarojeles resz - group(1) - lesz maga az UUID).
    private static final Pattern LOBBY_TOPIC_PATTERN = Pattern.compile("^/topic/lobbies/([0-9a-fA-F-]{36})$");

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final LobbyRepository lobbyRepository;
    private final LobbyParticipantRepository lobbyParticipantRepository;

    // preSend: a ChannelInterceptor interface fo metodusa, minden STOMP frame-nel (CONNECT,
    // SUBSCRIBE, SEND, DISCONNECT, stb.) lefut, MIELOTT az uzenet tovabbmenne a csatornaban.
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        // A STOMP-specifikus fejleceket (parancs, cel-cim, sajat headerek) ebbol az
        // "accessor" objektumbol lehet kiolvasni - ha null, ez nem is egy STOMP frame (nem
        // kellene elofordulnia, de biztonsagi ellenorzeskent kezeljuk).
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            requireLobbyChatAccess(accessor);
        }
        // Minden mas parancsnal (pl. SEND) nincs itt tovabbi teendo - a SEND uzenetek
        // jogosultsagat maga a ChatWebSocketController.sendMessage -> ChatMessageService
        // ellenorzi ujra, amikor ténylegesen menteni probalja az uzenetet.

        return message;
    }

    // Az elso kapcsolodaskor (CONNECT) fut le - kicsomagolja es ellenorzi a JWT-t, majd
    // beallitja a hitelesitett felhasznalot az accessor-on, hogy a KESOBBI (ugyanazon a
    // WebSocket-kapcsolaton erkezo) uzenetek mar tudjak, ki kuldte oket.
    private void authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // MessagingException itt: STOMP-vilagban ez a HTTP 401-nek megfelelo dontes -
            // a kapcsolat elutasitasra kerul, a kliens nem tud csatlakozni.
            throw new MessagingException("Hianyzo Authorization fejlec a WS CONNECT-ben.");
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            String email = jwtService.extractEmail(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new MessagingException("Ervenytelen vagy lejart JWT token.");
            }
            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            // Ez a legfontosabb sor: az accessor-ra allitott "user" lesz az, amit kesobb a
            // @MessageMapping metodusok Principal parametere fog megkapni (lasd
            // ChatWebSocketController.sendMessage).
            accessor.setUser(authentication);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new MessagingException("Ervenytelen JWT token a WS kapcsolathoz.", ex);
        }
    }

    // Minden feliratkozasnal (SUBSCRIBE) fut le - ha a cel-cim egy "/topic/lobbies/{id}"
    // mintaju lobby-chat topic, ellenorzi, hogy a mar hitelesitett felhasznalo tenyleg
    // jogosult-e olvasni azt a konkret lobbyt.
    private void requireLobbyChatAccess(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher matcher = LOBBY_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return; // nem lobby-chat topic, nincs mit ellenorizni
        }

        UUID lobbyId = UUID.fromString(matcher.group(1));
        UUID userId = extractUserId(accessor.getUser());

        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new MessagingException("Nincs ilyen lobby."));

        // Ugyanaz a jogosultsagi szabaly, mint a ChatMessageService.requireChatAccess-ben
        // (REST oldalon) - szandekosan kulon van irva, mert ez a websocket csomagban van,
        // es nem akarjuk a ket reteget (WS interceptor / service) osszekapcsolni csak emiatt.
        boolean isCreator = lobby.getCreator().getId().equals(userId);
        boolean isAcceptedParticipant = lobbyParticipantRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .map(participant -> participant.getStatus() == ParticipantStatus.ACCEPTED)
                .orElse(false);

        if (!isCreator && !isAcceptedParticipant) {
            throw new MessagingException("Csak a lobby szervezoje vagy elfogadott resztvevoi feliratkozhatnak a chatre.");
        }
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new MessagingException("Nincs hitelesitve a WS kapcsolat.");
    }
}
