package com.botlobby.backend.websocket;

import com.botlobby.backend.security.SecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Egyetlen STOMP handshake endpoint (/ws) + lobby-nkenti logikai csatornak:
 *  - kuldes:  /app/lobbies/{lobbyId}/chat.send
 *  - feliratkozas: /topic/lobbies/{lobbyId}
 * (A REST vazlat "/ws/lobbies/{id}/chat" jelolese ezt a logikai utvonalat irja le -
 * STOMP-ban nem kulon fizikai endpoint lobbynkent, hanem destination-alapu routing.)
 *
 * STOMP (Simple Text Oriented Messaging Protocol): egy uzenetkuldesi protokoll, ami a nyers
 * WebSocket fole epul, es "destination" (cel-cim) alapu uzenetkuldest/feliratkozast tesz
 * lehetove (hasonloan egy uzenetsorhoz/pub-sub rendszerhez) - ahelyett, hogy minden
 * kliens-szerver kommunikaciohoz sajat, kezzel irt uzenet-formatumot kellene keszíteni.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Value("${botlobby.cors.allowed-origins}")
    private String allowedOrigins;

    // A "/ws" a WebSocket-kapcsolat egyetlen belepesi pontja (a frontend api/ws.js ide
    // csatlakozik). .withSockJS(): a SockJS konyvtar automatikus tartalek-mechanizmusokat
    // ad (pl. HTTP long-polling), ha a bongeszo/halozat valamiert nem tud valodi WebSocket
    // kapcsolatot letesiteni - igy a chat regebbi/korlatozottabb kornyezetben is mukodik.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(SecurityConfig.parseAllowedOrigins(allowedOrigins).toArray(String[]::new))
                .withSockJS();
    }

    // A STOMP "uzenetbroker" bekapcsolasa - ez donti el, milyen cim-elotagok mit jelentenek:
    //  - enableSimpleBroker("/topic"): a "/topic/..." cimekre kuldott uzeneteket a szerver
    //    (memoriaban, egyszeru modon) szetosztja MINDEN feliratkozott kliensnek - ide erkeznek
    //    az elo chatuzenetek (lasd ChatWebSocketController.sendMessage).
    //  - setApplicationDestinationPrefixes("/app"): a "/app/..." cimre erkezo uzeneteket NEM
    //    a broker kezeli, hanem egy @MessageMapping-gel jelolt Java-metodusnak tovabbitja
    //    (lasd ChatWebSocketController.sendMessage @MessageMapping annotaciojat) - ez az
    //    "alkalmazas-logika" bemeneti pontja.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    // Minden bejovo STOMP uzenet (CONNECT, SUBSCRIBE, SEND, stb.) elobb ezen az
    // interceptor-on megy at, mielott celba erne - itt tortenik a JWT-alapu hitelesites es
    // a lobby-chat jogosultsag-ellenorzes (lasd StompAuthChannelInterceptor).
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
