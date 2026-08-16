import { useState } from 'react';
import { api } from '../api/client.js';
import { RatingStarsInput } from './RatingStars.jsx';
import ErrorBanner from './ErrorBanner.jsx';
import BottomSheet from '../motion/BottomSheet.jsx';
import MotionButton from '../motion/MotionButton.jsx';
import Switch from '../motion/Switch.jsx';

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
    <BottomSheet open onClose={onClose} title={`${targetUser.name} ertekelese`}>
      <form className="rating-sheet-form" onSubmit={handleSubmit}>
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

        <Switch checked={noShow} onChange={setNoShow} label="Nem jelent meg" />

        <div className="sheet-actions">
          <MotionButton type="button" className="btn-ghost" onClick={onClose}>
            Megse
          </MotionButton>
          <MotionButton type="submit" className="btn-primary" disabled={submitting}>
            {submitting ? 'Kuldes...' : 'Ertekeles kuldese'}
          </MotionButton>
        </div>
      </form>
    </BottomSheet>
  );
}
