package com.botlobby.backend.websocket;

import com.botlobby.backend.security.CustomUserDetailsService;
import com.botlobby.backend.security.JwtService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * A STOMP CONNECT frame "Authorization: Bearer <token>" native headerebol hitelesit -
 * a SecurityConfig HTTP szinten permitAll-ozza a /ws/** handshake-et, az uzenet-szintu
 * auth-ot itt vegezzuk. Sikeres hitelesites utan a StompHeaderAccessor-on beallitott
 * user a tovabbi @MessageMapping metodusokban Principal-kent erheto el.
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
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
                accessor.setUser(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                throw new MessagingException("Ervenytelen JWT token a WS kapcsolathoz.", ex);
            }
        }

        return message;
    }
}
