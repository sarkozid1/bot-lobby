import { Link, useLocation } from 'react-router-dom';
import { motion, useReducedMotion } from 'framer-motion';
import { LobbyStatusBadge, SkillBadge } from './Badge.jsx';
import { IconCalendar, IconUsers } from './Icon.jsx';
import { springs, withReducedMotion } from '../motion/springs.js';

// motion.create(Link): a React Router <Link>-bol keszit egy Framer Motion-kepes valtozatot
// (whileTap, layoutId, stb. propokkal) - igy a kartya EGYSZERRE navigacios link ES animalt elem.
const MotionLink = motion.create(Link);

// A LobbiesPage racsaban megjeleno egy-egy lobby "kartyaja". Ez a kartya a "forrasa" a
// kartyabol-nyilo-modal atmenetnek: a layoutId (lasd lent) osszekoti ezt az elemet a
// LobbyDetailModal ugyanolyan layoutId-javal, es a Framer Motion automatikusan animalja
// at az egyiket a masikba kattintaskor (shared-element/"card morph" atmenet).
export default function LobbyCard({ lobby }) {
  const location = useLocation();
  const reduced = useReducedMotion();
  const start = new Date(lobby.startTime);
  const isFull = lobby.currentParticipants >= lobby.maxParticipants;

  return (
    <MotionLink
      to={`/lobbies/${lobby.id}`}
      // A modal-route minta kulcsa: a JELENLEGI location-t (a lobby-lista oldalt) atadjuk
      // "backgroundLocation"-kent a navigacios allapotban - lasd App.jsx, ahol ez donti el,
      // hogy a lista a hatterben marad-e, mig felette egy modal nyilik meg.
      state={{ backgroundLocation: location }}
      className="lobby-card"
      // A layoutId EGYEZIK a LobbyDetailModal-ban hasznalt layoutId-vel (lasd ott) - ez a
      // "kozos elem" azonositoja, ami alapjan a Framer Motion tudja, melyik ket elem kozott
      // kell animalnia a meret/pozicio valtozasat.
      layoutId={`lobby-card-${lobby.id}`}
      whileTap={{ scale: 0.97 }}
      transition={withReducedMotion(springs.press, reduced)}
    >
      <div className="lobby-card-header">
        {/* Nincs kulon sport-ikon-kep a backendben hasznalva - a sport nevenek elso betuje
            szolgal minimalista, sajat generalt "ikonkent". */}
        <span className="lobby-sport-icon">{lobby.sport?.name?.[0]?.toUpperCase() || '?'}</span>
        <div className="lobby-card-heading">
          <h3>{lobby.title}</h3>
          <p className="lobby-card-sub">
            {lobby.sport?.name} · {lobby.city}
          </p>
        </div>
        <LobbyStatusBadge status={lobby.status} />
      </div>

      <div className="lobby-card-body">
        <span className="lobby-card-stat">
          <IconCalendar size={14} />
          {start.toLocaleString('hu-HU', { dateStyle: 'medium', timeStyle: 'short' })}
        </span>
        <span className={isFull ? 'lobby-card-stat lobby-slots-full' : 'lobby-card-stat'}>
          <IconUsers size={14} />
          {lobby.currentParticipants}/{lobby.maxParticipants}
        </span>
        {lobby.minSkillLevel && <SkillBadge level={lobby.minSkillLevel} />}
      </div>

      <div className="lobby-card-footer">Szervezo: {lobby.creator?.name}</div>
    </MotionLink>
  );
}
