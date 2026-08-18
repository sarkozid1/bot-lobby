import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from './springs.js';

// Sajat, iOS-stilusu kapcsolo komponens - NEM a natic <input type="checkbox">-ot hasznalja
// vizualisan (az csak "hidden"-kent van jelen lent, akadalymentesitesi/form-celokra), hanem
// egy kezzel epitett, animalt csuszka. Lasd RatingModal.jsx-ben a "Nem jelent meg" kapcsolonal.
export default function Switch({ checked, onChange, label }) {
  const reduced = useReducedMotion();

  return (
    // A <label> maga is kattinthato/fokuszalhato konteinerkent szolgal - ez akadalymentesitesi
    // szempontbol is jo gyakorlat (a cimke kattintasa is aktivalja a mezot).
    <label className="switch-row">
      {label && <span>{label}</span>}
      <span
        className={`switch ${checked ? 'switch-on' : ''}`}
        // ARIA attributumok: mivel ez NEM egy natic <input type="checkbox">, kepernyoolvaso
        // szoftvereknek expliciten meg kell mondani, hogy ez egy kapcsolo (role="switch"), es
        // mi az aktualis allapota (aria-checked).
        role="switch"
        aria-checked={checked}
        // tabIndex={0}: billentyuzettel is fokuszalhato legyen (alapertelmezetten egy <span>
        // nem az) - a lenti onKeyDown kezeli az Enter/Szokoz billentyus aktivalast.
        tabIndex={0}
        onClick={() => onChange(!checked)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onChange(!checked);
          }
        }}
      >
        {/* A mozgo "gomb" resz - x: 18 -> jobbra csusztatva, amikor bekapcsolt allapotban van.
            Csak ez a kis elem animalodik spring-gel, a hatterszin-valtas (switch-on osztaly)
            CSS transitionnel tortenik (lasd styles/index.css). */}
        <motion.span
          className="switch-knob"
          animate={{ x: checked ? 18 : 0 }}
          transition={withReducedMotion(springs.press, reduced)}
        />
      </span>
      {/* Rejtett natic checkbox - form-kontextusban (pl. ha valaha natic form submit-ot
          hasznalna a projekt) ez adna at az erteket, es segit a bongeszo beepitett
          akadalymentesitesi/automata-kitoltesi mechanizmusainak is. */}
      <input type="checkbox" checked={checked} onChange={(e) => onChange(e.target.checked)} hidden />
    </label>
  );
}
