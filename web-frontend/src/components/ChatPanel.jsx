import { useEffect, useRef, useState } from 'react';
import { AnimatePresence, motion, useReducedMotion } from 'framer-motion';
import { api } from '../api/client.js';
import { createChatConnection, sendChatMessage } from '../api/ws.js';
import { useAuth } from '../context/AuthContext.jsx';
import { springs, withReducedMotion } from '../motion/springs.js';
import MotionButton from '../motion/MotionButton.jsx';

// A lobby-reszletek oldal/modal "Chat" kartyaja - ez fogja ossze a REST-en betoltott
// elozmenyt es a WebSocket-en erkezo elo uzeneteket egyetlen, folyamatosan bovulo listava.
export default function ChatPanel({ lobbyId }) {
  const { user } = useAuth();
  const reduced = useReducedMotion();
  const [messages, setMessages] = useState([]);
  // false, ha a backend 403-at ad az elozmeny-lekerdezesre (csak szervezo/elfogadott tagok
  // chatelhetnek, lasd backend ChatMessageService.requireChatAccess).
  const [allowed, setAllowed] = useState(true);
  const [connected, setConnected] = useState(false);
  const [input, setInput] = useState('');
  // useRef: a STOMP kliens peldanyt es a scrollozhato lista DOM-elemet nem akarjuk, hogy
  // valtozasuk ujra-renderelest valtson ki (nem is React allapot, csak "kulso" referenciak).
  const clientRef = useRef(null);
  const listRef = useRef(null);

  // Ez az effect a lobbyId valtozasakor (pl. masik lobby chatjere valtaskor) ujra lefut:
  // eloszor betolti az elozmenyt REST-en, majd csak utana nyitja meg a WebSocket kapcsolatot.
  useEffect(() => {
    // "cancelled" zaszlo: ha a komponens unmountolodik (vagy lobbyId megvaltozik) MIELOTT a
    // fetch visszaterne, ne allitsunk be allapotot egy mar "elavult" valaszbol (elkeruli a
    // React "Can't perform a React state update on an unmounted component" figyelmeztetest,
    // es azt, hogy a regi lobby adatai felulirjak az uj lobbyet).
    let cancelled = false;

    api.lobbies
      .messages(lobbyId)
      .then((history) => {
        if (cancelled) return;
        setMessages(history);
        setAllowed(true);

        // Csak SIKERES elozmeny-betoltes UTAN nyitjuk meg a WebSocket kapcsolatot - ha a
        // felhasznalonak nincs jogosultsaga (403), nincs ertelme a kapcsolatot is felepiteni.
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

    // Takaritas: amikor a komponens elbucsuzik EBBEN a lobbyId-ban (unmount vagy lobbyId
    // valtozas), lezarjuk a WebSocket kapcsolatot - kulonben megnyilt kapcsolatok
    // halmozodnanak fel a hatterben minden lobby-valtaskor.
    return () => {
      cancelled = true;
      clientRef.current?.deactivate();
      clientRef.current = null;
    };
  }, [lobbyId]);

  // Minden uj uzenetnel automatikusan legorditi a listat az aljara, hogy a legfrissebb
  // uzenet mindig lathato legyen (mint egy normal chat-alkalmazasban).
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
      <div className="chat-status">
        <span className={connected ? 'chat-status-dot chat-status-dot-on' : 'chat-status-dot'} />
        {connected ? 'Elo kapcsolat' : 'Csatlakozas...'}
      </div>

      <ul className="chat-messages" ref={listRef}>
        {messages.length === 0 && <li className="chat-empty">Meg nincs uzenet - kezdd el a beszelgetest!</li>}
        {/* initial=false: a mar betoltott elozmenyek nem "csapodnak be" egyszerre - csak az
            elo, ujonnan erkezo uzenetek animalodnak be alulrol, spring-mel. */}
        <AnimatePresence initial={false}>
          {messages.map((m) => (
            <motion.li
              key={m.id}
              // layout: ha a lista magassaga valtozik (uj sor beker), a tobbi elem is
              // simán, animalva "csuszik at" az uj pozíciojaba, nem ugrik hirtelen.
              layout
              initial={{ opacity: 0, y: 16, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              transition={withReducedMotion(springs.structural, reduced)}
              // A sajat uzeneteink mas szinuek (jobbra igazitott, kek buborek), mint masoke -
              // lasd styles/index.css .chat-msg-own szabalyat.
              className={m.sender?.id === user?.id ? 'chat-msg chat-msg-own' : 'chat-msg'}
            >
              <span className="chat-msg-author">{m.sender?.name}</span>
              <span className="chat-msg-content">{m.content}</span>
            </motion.li>
          ))}
        </AnimatePresence>
      </ul>

      <form className="chat-input-row" onSubmit={handleSend}>
        <input value={input} onChange={(e) => setInput(e.target.value)} placeholder="Uzenet..." />
        {/* disabled amig nincs elo WebSocket kapcsolat - igy nem lehet olyan uzenetet
            elkuldeni, amit a szerver ugysem tudna fogadni. */}
        <MotionButton className="btn-primary" type="submit" disabled={!connected}>
          Kuldes
        </MotionButton>
      </form>
    </div>
  );
}
