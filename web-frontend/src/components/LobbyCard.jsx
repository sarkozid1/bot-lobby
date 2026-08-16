import { Link, useLocation } from 'react-router-dom';
import { motion, useReducedMotion } from 'framer-motion';
import { LobbyStatusBadge, SkillBadge } from './Badge.jsx';
import { IconCalendar, IconUsers } from './Icon.jsx';
import { springs, withReducedMotion } from '../motion/springs.js';

const MotionLink = motion.create(Link);

export default function LobbyCard({ lobby }) {
  const location = useLocation();
  const reduced = useReducedMotion();
  const start = new Date(lobby.startTime);
  const isFull = lobby.currentParticipants >= lobby.maxParticipants;

  return (
    <MotionLink
      to={`/lobbies/${lobby.id}`}
      state={{ backgroundLocation: location }}
      className="lobby-card"
      layoutId={`lobby-card-${lobby.id}`}
      whileTap={{ scale: 0.97 }}
      transition={withReducedMotion(springs.press, reduced)}
    >
      <div className="lobby-card-header">
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
