import { useState } from 'react';
import { api } from '../api/client.js';
import { RatingStarsInput } from './RatingStars.jsx';
import ErrorBanner from './ErrorBanner.jsx';

export default function RatingModal({ lobbyId, targetUser, onClose, onSubmitted }) {
  const [stars, setStars] = useState(5);
  const [tags, setTags] = useState('');
  const [comment, setComment] = useState('');
  const [noShow, setNoShow] = useState(false);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.lobbies.rate(lobbyId, {
        ratedUserId: targetUser.id,
        stars,
        tags: tags
          .split(',')
          .map((t) => t.trim())
          .filter(Boolean),
        comment: comment || null,
        noShow
      });
      onSubmitted?.();
      onClose();
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal card" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <h2>{targetUser.name} ertekelese</h2>
        <ErrorBanner error={error} />

        <label>
          Csillagok
          <RatingStarsInput value={stars} onChange={setStars} />
        </label>

        <label>
          Cimkek (vesszovel elvalasztva)
          <input value={tags} onChange={(e) => setTags(e.target.value)} placeholder="pontos, baratsagos" />
        </label>

        <label>
          Komment
          <textarea value={comment} onChange={(e) => setComment(e.target.value)} />
        </label>

        <label className="checkbox-label">
          <input type="checkbox" checked={noShow} onChange={(e) => setNoShow(e.target.checked)} />
          Nem jelent meg
        </label>

        <div className="modal-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Megse
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'Kuldes...' : 'Ertekeles kuldese'}
          </button>
        </div>
      </form>
    </div>
  );
}
