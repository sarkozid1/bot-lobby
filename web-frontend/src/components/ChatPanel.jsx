import { useEffect, useRef, useState } from 'react';
import { api } from '../api/client.js';
import { createChatConnection, sendChatMessage } from '../api/ws.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function ChatPanel({ lobbyId }) {
  const { user } = useAuth();
  const [messages, setMessages] = useState([]);
  const [allowed, setAllowed] = useState(true);
  const [connected, setConnected] = useState(false);
  const [input, setInput] = useState('');
  const clientRef = useRef(null);
  const listRef = useRef(null);

  useEffect(() => {
    let cancelled = false;

    api.lobbies
      .messages(lobbyId)
      .then((history) => {
        if (cancelled) return;
        setMessages(history);
        setAllowed(true);

        const client = createChatConnection(lobbyId, {
          onMessage: (msg) => setMessages((prev) => [...prev, msg]),
          onConnected: () => setConnected(true),
          onDisconnected: () => setConnected(false),
          onError: (msg) => {
            // eslint-disable-next-line no-console
            console.warn('Chat hiba:', msg);
          }
        });
        clientRef.current = client;
      })
      .catch((err) => {
        if (err.status === 403) setAllowed(false);
      });

    return () => {
      cancelled = true;
      clientRef.current?.deactivate();
      clientRef.current = null;
    };
  }, [lobbyId]);

  useEffect(() => {
    listRef.current?.scrollTo({ top: listRef.current.scrollHeight });
  }, [messages]);

  function handleSend(e) {
    e.preventDefault();
    if (!input.trim() || !clientRef.current) return;
    sendChatMessage(clientRef.current, lobbyId, input.trim());
    setInput('');
  }

  if (!allowed) {
    return <p className="empty-state">A chat csak a szervezo es az elfogadott resztvevok szamara erheto el.</p>;
  }

  return (
    <div className="chat-panel">
      <div className="chat-status">{connected ? '🟢 Elo kapcsolat' : '🟡 Csatlakozas...'}</div>

      <ul className="chat-messages" ref={listRef}>
        {messages.length === 0 && <li className="chat-empty">Meg nincs uzenet - kezdd el a beszelgetest!</li>}
        {messages.map((m) => (
          <li key={m.id} className={m.sender?.id === user?.id ? 'chat-msg chat-msg-own' : 'chat-msg'}>
            <span className="chat-msg-author">{m.sender?.name}</span>
            <span className="chat-msg-content">{m.content}</span>
          </li>
        ))}
      </ul>

      <form className="chat-input-row" onSubmit={handleSend}>
        <input value={input} onChange={(e) => setInput(e.target.value)} placeholder="Uzenet..." />
        <button className="btn btn-primary" type="submit" disabled={!connected}>
          Kuldes
        </button>
      </form>
    </div>
  );
}
