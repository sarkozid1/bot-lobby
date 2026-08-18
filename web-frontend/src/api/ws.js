// A lobby-chat valos idejo (WebSocket/STOMP) resze - ez KULON csatorna a tobbi API-hivastol
// (lasd api/client.js), mert itt nem egyszeri kerdes-valasz, hanem egy folyamatosan nyitva
// tartott kapcsolat kell, amin barmikor erkezhet uzenet a szervertol.
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getBaseUrl, getToken } from './client';

// Letrehoz es azonnal aktival egy STOMP kapcsolatot egy adott lobby chatjehez. A hivo
// (ChatPanel.jsx) callback-eket ad at a kulonbozo esemenyekhez (uj uzenet, kapcsolodott/
// lecsatlakozott, hiba) - ez a fuggveny maga NEM dont semmirol, csak tovabbitja az esemenyeket.
export function createChatConnection(lobbyId, { onMessage, onConnected, onDisconnected, onError }) {
  const client = new Client({
    // SockJS: a nyers WebSocket helyett SockJS-en keresztul csatlakozunk (ugyanaz a
    // konyvtar, amit a backend WebSocketConfig .withSockJS()-e vár) - automatikus tartalek-
    // mechanizmusokat ad, ha a kozvetlen WebSocket kapcsolat nem mukodne.
    webSocketFactory: () => new SockJS(`${getBaseUrl()}/ws`),
    // A JWT-t itt, a STOMP CONNECT frame native headerjeben kuldjuk - ezt olvassa ki es
    // ellenorzi a backend StompAuthChannelInterceptor.authenticate.
    connectHeaders: { Authorization: `Bearer ${getToken()}` },
    // Ha megszakad a kapcsolat, 4 masodperc mulva a konyvtar automatikusan ujraprobalkozik.
    reconnectDelay: 4000,
    // "Szivveres" uzenetek mindket iranyban 10 masodpercenkent - ezekbol tudja mindket fel
    // eszrevenni, ha a masik oldal csendben, hiba nelkul eltunt (pl. halozati megszakadas).
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    // Kikapcsolt debug-naploz as (alapertelmezetten a konyvtar a konzolra irna minden STOMP
    // frame-et - ez itt szandekosan nemita el).
    debug: () => {},
    onConnect: () => {
      // Feliratkozas az adott lobby "csatornajara" - minden ide erkezo uzenetet (amit a
      // backend ChatWebSocketController.sendMessage szor ki MINDEN feliratkozottnak) itt
      // kapunk meg. A backend StompAuthChannelInterceptor.requireLobbyChatAccess ellenorzi,
      // hogy jogosultak vagyunk-e egyaltalan feliratkozni erre a topicra.
      client.subscribe(`/topic/lobbies/${lobbyId}`, (message) => {
        try {
          onMessage(JSON.parse(message.body));
        } catch {
          // eslint-disable-next-line no-console
          console.warn('Nem JSON chat uzenet erkezett:', message.body);
        }
      });
      // A csak-nekunk-szolo hibauzenetek csatornaja (lasd backend
      // ChatWebSocketController.handleException @SendToUser("/queue/errors") annotaciojat).
      client.subscribe('/user/queue/errors', (message) => {
        onError?.(message.body);
      });
      onConnected?.();
    },
    onWebSocketClose: () => onDisconnected?.(),
    onStompError: (frame) => onError?.(frame.headers?.message || 'STOMP hiba tortent.'),
    onWebSocketError: () => onError?.('Nem sikerult felepiteni a WebSocket kapcsolatot.')
  });

  // A Client létrehozasa onmagaban meg nem nyit kapcsolatot - az .activate() inditja el
  // ténylegesen a csatlakozasi folyamatot (es minden kesobbi ujracsatlakozast is).
  client.activate();
  return client;
}

// Uj chatuzenet kuldese - ez a "/app/..." elotagu cimre megy (lasd backend
// WebSocketConfig.setApplicationDestinationPrefixes("/app")), ami a
// ChatWebSocketController.sendMessage @MessageMapping metodusahoz iranyitja.
export function sendChatMessage(client, lobbyId, content) {
  client.publish({
    destination: `/app/lobbies/${lobbyId}/chat.send`,
    body: JSON.stringify({ content })
  });
}
