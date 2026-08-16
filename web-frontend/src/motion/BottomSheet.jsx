import { motion, useReducedMotion } from 'framer-motion';
import { springs, withReducedMotion } from './springs.js';

/**
 * Translucens, huzhato bottom sheet.
 * - drag="y" + dragConstraints + dragElastic: 1:1 kovetes, felfele nem enged (mar nyitva van),
 *   lefele rubber-band ellenallassal.
 * - Framer Motion beepitett drag-inertia-ja vegzi a momentum-projekciot elengedeskor.
 * - Megszakithato: ha a nyitas/zaras animacio kozben megfogjak, a drag automatikusan
 *   a jelenlegi (presentation) ertektol folytatja - ez a Framer Motion default viselkedese.
 *
 * Szandekosan MINDIG a DOM-ban marad (nincs AnimatePresence-alapu unmount) - `animate`
 * targetkent kapja meg a nyitott/zart allapotot. Ez animacio-fuggetlen biztonsagi halo:
 * a pointer-events/visibility minden renderben SZINKRON, kozvetlenul az `open` propbol
 * szamolodik, nem az animacio lezarulasatol fugg - igy egy esetleg sose befejezodo
 * (pl. nem komponalodo/hatterben levo lap) animacio sem tudna lathatatlanul
 * kattinthatatlanna tenni az oldalt.
 */
export default function BottomSheet({ open, onClose, title, children }) {
  const reduced = useReducedMotion();

  function handleDragEnd(_event, info) {
    const shouldClose = info.velocity.y > 500 || (info.offset.y > 120 && info.velocity.y >= 0);
    if (shouldClose) {
      onClose();
    }
    // kulonben a dragConstraints + beepitett inercia magatol visszahuzza 0-ra
  }

  return (
    <>
      <motion.div
        className="sheet-backdrop"
        style={{ pointerEvents: open ? 'auto' : 'none', visibility: open ? 'visible' : 'hidden' }}
        animate={{ opacity: open ? 1 : 0 }}
        transition={withReducedMotion(springs.structuralFast, reduced)}
        onClick={onClose}
      />
      <motion.div
        className="sheet"
        role="dialog"
        aria-modal="true"
        aria-hidden={!open}
        style={{ pointerEvents: open ? 'auto' : 'none', visibility: open ? 'visible' : 'hidden' }}
        animate={{ y: open ? 0 : '100%' }}
        transition={withReducedMotion(springs.sheet, reduced)}
        drag={reduced || !open ? false : 'y'}
        dragConstraints={{ top: 0, bottom: 0 }}
        dragElastic={{ top: 0, bottom: 0.55 }}
        onDragEnd={handleDragEnd}
      >
        <div className="sheet-handle" />
        {title && <h2 className="sheet-title">{title}</h2>}
        <div className="sheet-content">{children}</div>
      </motion.div>
    </>
  );
}
