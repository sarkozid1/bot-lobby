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
 *
 * Ezt hasznalja a LobbiesPage szuro-urlapja, a "Uj lobby" urlap, es a RatingModal is -
 * altalanos celu, ujrafelhasznalhato "felugro urlap" konteiner.
 */
export default function BottomSheet({ open, onClose, title, children }) {
  const reduced = useReducedMotion();

  // Huzas vege: itt dol el, hogy a felhasznalo "eleg messze/eleg gyorsan" huzta-e le a
  // sheet-et ahhoz, hogy azt zarasi szandeknak tekintsuk.
  function handleDragEnd(_event, info) {
    // Zaras, HA: gyors, lefele iranyulo "flick" gesztus (velocity.y > 500 px/s) VAGY a
    // felhasznalo mar tobb mint 120px-et huzott le ES nem lendul epp visszafele (velocity.y >= 0).
    const shouldClose = info.velocity.y > 500 || (info.offset.y > 120 && info.velocity.y >= 0);
    if (shouldClose) {
      onClose();
    }
    // kulonben a dragConstraints + beepitett inercia magatol visszahuzza 0-ra
  }

  return (
    <>
      {/* Hatter-elsotetites (scrim) - kattintasra is bezarja a sheet-et. */}
      <motion.div
        className="sheet-backdrop"
        // Zart allapotban NEM csak lathatatlan (opacity: 0), hanem pointer-events: 'none' es
        // visibility: 'hidden' is - igy zart allapotban semmikepp nem tud a lathatatlan
        // hatter-reteg kattintasokat elnyelni a mogotte levo tartalom elol.
        style={{ pointerEvents: open ? 'auto' : 'none', visibility: open ? 'visible' : 'hidden' }}
        animate={{ opacity: open ? 1 : 0 }}
        transition={withReducedMotion(springs.structuralFast, reduced)}
        onClick={onClose}
      />
      <motion.div
        className="sheet"
        // Akadalymentesitesi attributumok: jelzik a kepernyoolvasoknak, hogy ez egy modalis
        // parbeszedablak, es hogy jelenleg lathato-e.
        role="dialog"
        aria-modal="true"
        aria-hidden={!open}
        style={{ pointerEvents: open ? 'auto' : 'none', visibility: open ? 'visible' : 'hidden' }}
        // A tenyleges nyitas/zaras animacio: y: 0 (a kepernyo aljara illesztve, teljesen
        // lathatoan) vagy y: '100%' (teljesen a kepernyo alatt, lathatatlanul).
        animate={{ y: open ? 0 : '100%' }}
        transition={withReducedMotion(springs.sheet, reduced)}
        // Huzhatosag: csak akkor engedelyezett, ha NINCS reduced-motion ES a sheet epp NYITVA
        // van (zart sheet-et nem lehet "elohuzni" gesztussal, csak az onClose/nyito gombbal).
        drag={reduced || !open ? false : 'y'}
        // Felfele egyaltalan nem enged huzni (top: 0 - mar a "teljesen nyitott" pozicioban
        // van), lefele viszont igen, 0.55-os rugalmassaggal (rubber-band erzet).
        dragConstraints={{ top: 0, bottom: 0 }}
        dragElastic={{ top: 0, bottom: 0.55 }}
        onDragEnd={handleDragEnd}
      >
        {/* Vizualis "fogantyu" - jelzi a felhasznalonak, hogy ez a sheet lehuzhato. */}
        <div className="sheet-handle" />
        {title && <h2 className="sheet-title">{title}</h2>}
        {/* A tenyleges tartalom (pl. egy urlap) - a hivo komponens (pl. LobbiesPage) adja at
            children-kent, ez a BottomSheet maga nem tudja, mi van benne. */}
        <div className="sheet-content">{children}</div>
      </motion.div>
    </>
  );
}
