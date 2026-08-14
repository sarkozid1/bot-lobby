import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getBaseUrl, getToken } from './client';

export function createChatConnection(lobbyId, { onMessage, onConnected, onDisconnected, onError }) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${getBaseUrl()}/ws`),
    connectHeaders: { Authorization: `Bearer ${getToken()}` },
    reconnectDelay: 4000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    debug: () => {},
    onConnect: () => {
      client.subscribe(`/topic/lobbies/${lobbyId}`, (message) => {
        try {
          onMessage(JSON.parse(message.body));
        } catch {
          // eslint-disable-next-line no-console
          console.warn('Nem JSON chat uzenet erkezett:', message.body);
        }
      });
      client.subscribe('/user/queue/errors', (message) => {
        onError?.(message.body);
      });
      onConnected?.();
    },
    onWebSocketClose: () => onDisconnected?.(),
    onStompError: (frame) => onError?.(frame.headers?.message || 'STOMP hiba tortent.'),
    onWebSocketError: () => onError?.('Nem sikerult felepiteni a WebSocket kapcsolatot.')
  });

  client.activate();
  return client;
}

export function sendChatMessage(client, lobbyId, content) {
  client.publish({
    destination: `/app/lobbies/${lobbyId}/chat.send`,
    body: JSON.stringify({ content })
  });
}
