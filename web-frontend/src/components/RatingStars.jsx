import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from '../motion/springs.js';

// Ket kulon komponens: az egyik CSAK MEGJELENIT egy csillag-ertekelest (olvasas), a masik
// interaktiv BEVITELI mezo (irhato/kattinthato) - szandekosan nem ugyanaz a komponens ket
// modban, mert a ket hasznalati eset (statikus kijelzes vs. urlap-bevitel) elég kulonbozo.

// Read-only megjelenites - pl. a profiloldalon a megbizhatosagi pontszam mellett.
export function RatingStarsDisplay({ value, size = 'md' }) {
  // A backend tizedestort ertekeket ad (pl. 4.67) - itt FELCSILLAGRA kerekitjuk (a *2/2
  // trukk: szorzas 2-vel, kerekites egesz szamra, majd osztas 2-vel = legkozelebbi 0.5-re
  // kerekites), hogy a csillagok vizualisan is arányosan legyenek kitoltve/uresek.
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

// Interaktiv, kattinthato csillag-bevitel - a RatingModal "hany csillagot adsz" mezojehez.
export function RatingStarsInput({ value, onChange }) {
  const reduced = useReducedMotion();

  return (
    <span className="stars stars-input">
      {/* Ot gomb, mindegyik a sajat sorszamat (1-5) kuldi vissza kattintaskor - "n <= value"
          donti el, hogy az adott csillag "kitoltottnek" (mar kivalasztottnak) latszik-e. */}
      {[1, 2, 3, 4, 5].map((n) => (
        <motion.button
          type="button"
          key={n}
          className={n <= value ? 'star star-filled' : 'star'}
          onClick={() => onChange(n)}
          // Nagyobb "pattanas" (1.35x), mint a legtobb tobbi gombnal (whileTap 0.96) -
          // szandekosan hangsulyosabb visszajelzes egy jatekos, ertekeles-jellegu interakcional.
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
