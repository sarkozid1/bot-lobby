import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from './springs.js';

export default function Switch({ checked, onChange, label }) {
  const reduced = useReducedMotion();

  return (
    <label className="switch-row">
      {label && <span>{label}</span>}
      <span
        className={`switch ${checked ? 'switch-on' : ''}`}
        role="switch"
        aria-checked={checked}
        tabIndex={0}
        onClick={() => onChange(!checked)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onChange(!checked);
          }
        }}
      >
        <motion.span
          className="switch-knob"
          animate={{ x: checked ? 18 : 0 }}
          transition={withReducedMotion(springs.press, reduced)}
        />
      </span>
      <input type="checkbox" checked={checked} onChange={(e) => onChange(e.target.checked)} hidden />
    </label>
  );
}
