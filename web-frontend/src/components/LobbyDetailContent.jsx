import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { LobbyStatusBadge, SkillBadge } from './Badge.jsx';
import ErrorBanner from './ErrorBanner.jsx';
import ChatPanel from './ChatPanel.jsx';
import RatingModal from './RatingModal.jsx';
import SwipeableParticipantRow from './SwipeableParticipantRow.jsx';
import MotionButton from '../motion/MotionButton.jsx';

/**
 * A lobby-reszletek tenyleges tartalma, ujrafelhasznalva a teljes oldalas
 * nezetben (LobbyDetailPage) es a kartyabol nyilo modalban (LobbyDetailModal) is.
 */
export default function LobbyDetailContent({ id }) {
  const { user } = useAuth();
  const toast = useToast();

  const [lobby, setLobby] = useState(null);
  const [participants, setParticipants] = useState([]);
  const [participantsVisible, setParticipantsVisible] = useState(true);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionBusy, setActionBusy] = useState(false);
  const [rateTarget, setRateTarget] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const lobbyData = await api.lobbies.get(id);
      setLobby(lobbyData);

      try {
        const p = await api.lobbies.participants(id);
        setParticipants(p);
        setParticipantsVisible(true);
      } catch (err) {
        if (err.status === 403) {
          setParticipantsVisible(false);
          setParticipants([]);
        } else {
          throw err;
        }
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  async function runAction(fn, successMsg) {
    setActionBusy(true);
    try {
      await fn();
      if (successMsg) toast.success(successMsg);
      await load();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setActionBusy(false);
    }
  }

  if (loading) return <div className="page-loading">Betoltes...</div>;
  if (error) return <ErrorBanner error={error} />;
  if (!lobby) return null;

  const isCreator = user && lobby.creator.id === user.id;
  const isPast = new Date(lobby.startTime) < new Date();
  const start = new Date(lobby.startTime);

  const acceptedParticipants = participants.filter((p) => p.status === 'ACCEPTED');
  const rateableUsers = [
    ...(isCreator ? [] : [lobby.creator]),
    ...acceptedParticipants.filter((p) => p.user.id !== user?.id).map((p) => p.user)
  ];

  return (
    <>
      <div className="card lobby-detail-header">
        <div className="lobby-detail-title-row">
          <h1>{lobby.title}</h1>
          <LobbyStatusBadge status={lobby.status} />
        </div>

        <p className="lobby-detail-meta">
          {lobby.sport.name} · {lobby.city} ·{' '}
          {start.toLocaleString('hu-HU', { dateStyle: 'full', timeStyle: 'short' })}
        </p>

        {lobby.minSkillLevel && <SkillBadge level={lobby.minSkillLevel} />}
        {lobby.description && <p className="lobby-detail-description">{lobby.description}</p>}

        <p className="lobby-detail-meta">
          Szervezo: <Link to={`/users/${lobby.creator.id}`}>{lobby.creator.name}</Link> · Letszam:{' '}
          {lobby.currentParticipants}/{lobby.maxParticipants}
        </p>

        <div className="lobby-detail-actions">
          {isCreator ? (
            <MotionButton
              className="btn-danger"
              disabled={actionBusy || lobby.status === 'CANCELLED'}
              onClick={() => {
                if (window.confirm('Biztosan lemondod ezt a lobbyt?')) {
                  runAction(() => api.lobbies.cancel(id), 'Lobby lemondva.');
                }
              }}
            >
              Lobby lemondasa
            </MotionButton>
          ) : (
            <>
              <MotionButton
                className="btn-primary"
                disabled={actionBusy || lobby.status !== 'OPEN'}
                onClick={() => runAction(() => api.lobbies.join(id), 'Jelentkezes elkuldve!')}
              >
                Csatlakozas
              </MotionButton>
              <MotionButton
                className="btn-ghost"
                disabled={actionBusy}
                onClick={() => runAction(() => api.lobbies.leave(id), 'Elhagytad a lobbyt.')}
              >
                Elhagyas
              </MotionButton>
            </>
          )}
        </div>
      </div>

      <div className="card">
        <h2>Reszvevok</h2>
        {!participantsVisible ? (
          <p className="empty-state">A reszvevo lista csak a szervezo es az elfogadott tagok szamara lathato.</p>
        ) : participants.length === 0 ? (
          <p className="empty-state">Meg senki nem jelentkezett.</p>
        ) : (
          <ul className="participant-list">
            {participants.map((p) =>
              isCreator && p.status === 'PENDING' ? (
                <SwipeableParticipantRow
                  key={p.id}
                  participant={p}
                  disabled={actionBusy}
                  onAccept={() => runAction(() => api.lobbies.accept(id, p.user.id), 'Jelentkezo elfogadva.')}
                  onDecline={() => runAction(() => api.lobbies.decline(id, p.user.id), 'Jelentkezo elutasitva.')}
                />
              ) : (
                <li key={p.id} className="participant-row">
                  <Link to={`/users/${p.user.id}`}>{p.user.name}</Link>
                  <span className={`badge badge-participant-${p.status.toLowerCase()}`}>
                    {{ PENDING: 'Fuggoben', ACCEPTED: 'Elfogadva', DECLINED: 'Elutasitva', LEFT: 'Kilepett' }[
                      p.status
                    ] || p.status}
                  </span>
                </li>
              )
            )}
          </ul>
        )}
      </div>

      <div className="card">
        <h2>Chat</h2>
        <ChatPanel lobbyId={id} />
      </div>

      {participantsVisible && isPast && rateableUsers.length > 0 && (
        <div className="card">
          <h2>Reszvevok ertekelese</h2>
          <ul className="rate-list">
            {rateableUsers.map((u) => (
              <li key={u.id} className="rate-list-row">
                <Link to={`/users/${u.id}`}>{u.name}</Link>
                <MotionButton className="btn-sm btn-secondary" onClick={() => setRateTarget(u)}>
                  Ertekeles
                </MotionButton>
              </li>
            ))}
          </ul>
        </div>
      )}

      {rateTarget && (
        <RatingModal
          lobbyId={id}
          targetUser={rateTarget}
          onClose={() => setRateTarget(null)}
          onSubmitted={() => toast.success('Ertekeles elkuldve!')}
        />
      )}
    </>
  );
}
