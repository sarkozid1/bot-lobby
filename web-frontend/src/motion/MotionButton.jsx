import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from './springs.js';

// Kozponti "zero-latency" gomb: whileTap pointer-downkor azonnal reagal (nem clickkor/felengedeskor),
// spring-mel, sosem CSS transitionnel.
// Ezt hasznalja szinte minden interaktiv gomb az appban (a sima <button className="btn">-ok
// helyett), hogy egysegesen erezzek magukat a "megnyomas" visszajelzesek.
export default function MotionButton({ className = '', children, disabled, ...props }) {
  // Ha a felhasznalo rendszerszinten kikapcsolta/csokkentette az animaciokat, ezt jelzi.
  const reduced = useReducedMotion();

  return (
    // motion.button: a Framer Motion "animalhato" valtozata a sima <button>-nak - minden
    // rendes HTML attributumot tovabb tud adni (onClick, type, stb. - lasd {...props}),
    // csak plusz animacios kepessegeket ad hozza.
    <motion.button
      // A kulso `className` prop mindig HOZZAFUZODIK az alap "btn" osztalyhoz (nem
      // FELULIRJA azt) - igy a hivo csak a szinvaltozathoz szukseges extra osztalyt adja
      // meg (pl. "btn-primary"), az alapstilus mindig megmarad.
      className={`btn ${className}`}
      disabled={disabled}
      // whileTap: MIHELYT a felhasznalo lenyomja (pointerdown), NEM amikor felengedi vagy
      // amikor a click esemeny lefut - ez adja a "zero-latency" erzetet, mert a vizualis
      // valasz nem var meg semmilyen tovabbi esemenyt. disabled gombnal nincs tap-animacio.
      whileTap={disabled ? undefined : { scale: 0.96 }}
      transition={withReducedMotion(springs.press, reduced)}
      {...props}
    >
      {children}
    </motion.button>
  );
}
