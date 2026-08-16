import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from '../motion/springs.js';

export function RatingStarsDisplay({ value, size = 'md' }) {
  const rounded = Math.round((value || 0) * 2) / 2;
  return (
    <span className={`stars stars-${size}`} title={`${value?.toFixed ? value.toFixed(2) : value} / 5`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= rounded ? 'star star-filled' : 'star'}>
          ★
        </span>
      ))}
    </span>
  );
}

export function RatingStarsInput({ value, onChange }) {
  const reduced = useReducedMotion();

  return (
    <span className="stars stars-input">
      {[1, 2, 3, 4, 5].map((n) => (
        <motion.button
          type="button"
          key={n}
          className={n <= value ? 'star star-filled' : 'star'}
          onClick={() => onChange(n)}
          whileTap={{ scale: 1.35 }}
          transition={withReducedMotion(springs.press, reduced)}
          aria-label={`${n} csillag`}
        >
          ★
        </motion.button>
      ))}
    </span>
  );
}
