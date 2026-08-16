import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from './springs.js';

// Kozponti "zero-latency" gomb: whileTap pointer-downkor azonnal reagal (nem clickkor/felengedeskor),
// spring-mel, sosem CSS transitionnel.
export default function MotionButton({ className = '', children, disabled, ...props }) {
  const reduced = useReducedMotion();

  return (
    <motion.button
      className={`btn ${className}`}
      disabled={disabled}
      whileTap={disabled ? undefined : { scale: 0.96 }}
      transition={withReducedMotion(springs.press, reduced)}
      {...props}
    >
      {children}
    </motion.button>
  );
}
