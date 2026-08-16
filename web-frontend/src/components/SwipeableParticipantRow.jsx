import { animate, motion, useMotionValue, useReducedMotion, useTransform } from 'framer-motion';
import { Link } from 'react-router-dom';
import { springs, withReducedMotion } from '../motion/springs.js';

const COMMIT_THRESHOLD = 90;

/**
 * Huzhato reszvevo-sor PENDING jelentkezokhoz: jobbra = elfogad, balra = elutasit.
 * A gombok mindig lathatoak maradnak is (nem csak gesztussal erheto el a muvelet -
 * fontos akcio sosem lehet kizarolag gesztus-fuggo).
 */
export default function SwipeableParticipantRow({ participant, onAccept, onDecline, disabled }) {
  const reduced = useReducedMotion();
  const x = useMotionValue(0);
  const acceptOpacity = useTransform(x, [0, COMMIT_THRESHOLD], [0, 1]);
  const declineOpacity = useTransform(x, [-COMMIT_THRESHOLD, 0], [1, 0]);

  function handleDragEnd(_event, info) {
    if (info.offset.x > COMMIT_THRESHOLD) {
      onAccept();
    } else if (info.offset.x < -COMMIT_THRESHOLD) {
      onDecline();
    }
    animate(x, 0, withReducedMotion(springs.sheet, reduced));
  }

  return (
    <li className="swipe-row">
      <div className="swipe-row-backdrop">
        <motion.span className="swipe-hint swipe-hint-accept" style={{ opacity: acceptOpacity }}>
          ✓ Elfogad
        </motion.span>
        <motion.span className="swipe-hint swipe-hint-decline" style={{ opacity: declineOpacity }}>
          ✕ Elutasit
        </motion.span>
      </div>

      <motion.div
        className="swipe-row-content participant-row"
        style={{ x }}
        drag={disabled || reduced ? false : 'x'}
        dragConstraints={{ left: -140, right: 140 }}
        dragElastic={0.2}
        onDragEnd={handleDragEnd}
      >
        <Link to={`/users/${participant.user.id}`}>{participant.user.name}</Link>
        <span className="badge badge-participant-pending">Fuggoben</span>
        <span className="participant-actions">
          <button type="button" className="btn btn-sm btn-primary" disabled={disabled} onClick={onAccept}>
            Elfogad
          </button>
          <button type="button" className="btn btn-sm btn-ghost" disabled={disabled} onClick={onDecline}>
            Elutasit
          </button>
        </span>
      </motion.div>
    </li>
  );
}
