import { animate, motion, useMotionValue, useReducedMotion, useTransform } from 'framer-motion';
import { Link } from 'react-router-dom';
import { springs, withReducedMotion } from '../motion/springs.js';

// Ilyen sok pixel elhuzas kell ahhoz, hogy egy jobbra/balra swipe "elkotelezettnek" (commit)
// szamitson, es tenylegesen kivaltsa az elfogadas/elutasitas akciot.
const COMMIT_THRESHOLD = 90;

/**
 * Huzhato reszvevo-sor PENDING jelentkezokhoz: jobbra = elfogad, balra = elutasit.
 * A gombok mindig lathatoak maradnak is (nem csak gesztussal erheto el a muvelet -
 * fontos akcio sosem lehet kizarolag gesztus-fuggo).
 */
export default function SwipeableParticipantRow({ participant, onAccept, onDecline, disabled }) {
  const reduced = useReducedMotion();
  // useMotionValue: egy Framer Motion-specifikus, React state-en KIVULI (nem ujra-
  // renderelo) ertek - a drag kozvetlenul ezt frissiti nagyon nagy frekvencian, React
  // ujra-renderelese nelkul (teljesitmeny-optimalizacio a folyamatos huzasi animaciohoz).
  const x = useMotionValue(0);
  // useTransform: szarmaztatott ertek - az "elfogad" felirat lathatosaga (opacity)
  // KOZVETLENUL az x elmozdulasbol szamolodik: 0-nal meg lathatatlan, COMMIT_THRESHOLD-nel
  // mar teljesen lathato - igy a felirat FOKOZATOSAN tunik fel huzas kozben, nem hirtelen.
  const acceptOpacity = useTransform(x, [0, COMMIT_THRESHOLD], [0, 1]);
  const declineOpacity = useTransform(x, [-COMMIT_THRESHOLD, 0], [1, 0]);

  // Huzas vege: itt dol el, elerte-e a felhasznalo a "commit" hatart barmelyik iranyba.
  function handleDragEnd(_event, info) {
    if (info.offset.x > COMMIT_THRESHOLD) {
      onAccept();
    } else if (info.offset.x < -COMMIT_THRESHOLD) {
      onDecline();
    }
    // Barmi is tortent (commit-alt vagy sem), a sor MINDIG visszaanimalodik a kozepre
    // (x=0) - animate(): kezzel inditott animacio a motion value-n, nem a JSX animate propon at.
    animate(x, 0, withReducedMotion(springs.sheet, reduced));
  }

  return (
    <li className="swipe-row">
      {/* Ez a reteg a HUZHATO tartalom MOGOTT all - csak akkor latszik, amikor a felso
          reteg elcsuszik felette (lasd .swipe-row-backdrop a CSS-ben, ami ket felre osztott
          zold/piros hattert ad). */}
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
        // Az x motion value-t kozvetlenul a CSS transform-hoz kotjuk - amig a felhasznalo
        // huzza, ez a sor vizualisan koveti az ujjat/egeret 1:1 aranyban.
        style={{ x }}
        // Letiltott allapotban VAGY reduced-motion alatt egyaltalan nem huzhato - ekkor
        // csak a mindig lathato Elfogad/Elutasit gombokkal lehet muveletet vegezni.
        drag={disabled || reduced ? false : 'x'}
        dragConstraints={{ left: -140, right: 140 }}
        dragElastic={0.2}
        onDragEnd={handleDragEnd}
      >
        <Link to={`/users/${participant.user.id}`}>{participant.user.name}</Link>
        <span className="badge badge-participant-pending">Fuggoben</span>
        {/* A gesztus MELLETT mindig ott vannak a hagyomanyos gombok is - igerteti a
            komponens fejlecen levo megjegyzest: "fontos akcio sosem lehet kizarolag
            gesztus-fuggo" (billentyuzet-hasznaloknak/erintomentes eszkozoknek is mukodnie kell). */}
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
