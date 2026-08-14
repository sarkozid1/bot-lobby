import { Link } from 'react-router-dom';
import { LobbyStatusBadge, SkillBadge } from './Badge.jsx';

export default function LobbyCard({ lobby }) {
  const start = new Date(lobby.startTime);
  const isFull = lobby.currentParticipants >= lobby.maxParticipants;

  return (
    <Link to={`/lobbies/${lobby.id}`} className="lobby-card">
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
        <span>📅 {start.toLocaleString('hu-HU', { dateStyle: 'medium', timeStyle: 'short' })}</span>
        <span className={isFull ? 'lobby-slots lobby-slots-full' : 'lobby-slots'}>
          👥 {lobby.currentParticipants}/{lobby.maxParticipants}
        </span>
        {lobby.minSkillLevel && <SkillBadge level={lobby.minSkillLevel} />}
      </div>

      <div className="lobby-card-footer">Szervezo: {lobby.creator?.name}</div>
    </Link>
  );
}
