import { AnimatePresence, motion, useReducedMotion } from 'framer-motion';
import { useTheme } from '../context/ThemeContext.jsx';
import { springs, withReducedMotion } from '../motion/springs.js';
import { IconMonitor, IconMoon, IconSun } from './Icon.jsx';

const ICONS = { system: IconMonitor, light: IconSun, dark: IconMoon };
const LABELS = { system: 'Rendszer', light: 'Vilagos', dark: 'Sotet' };

// Harmasallasu kapcsolo (rendszer -> vilagos -> sotet -> ...), az aktualis ikon
// spring-es cross-fade-del valtja egymast, ugyanazokkal a preszetekkel mint a tobbi
// motion-komponens (lasd motion/springs.js).
export default function ThemeToggle() {
  const { theme, cycleTheme } = useTheme();
  const reduced = useReducedMotion();
  const Icon = ICONS[theme];

  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={cycleTheme}
      title={`Megjelenes: ${LABELS[theme]} - kattints a valtashoz`}
      aria-label={`Megjelenes valtasa (jelenleg: ${LABELS[theme]})`}
    >
      <AnimatePresence mode="wait" initial={false}>
        <motion.span
          key={theme}
          className="theme-toggle-icon"
          initial={{ opacity: 0, rotate: -50, scale: 0.6 }}
          animate={{ opacity: 1, rotate: 0, scale: 1 }}
          exit={{ opacity: 0, rotate: 50, scale: 0.6 }}
          transition={withReducedMotion(springs.press, reduced)}
        >
          <Icon size={16} />
        </motion.span>
      </AnimatePresence>
    </button>
  );
}
