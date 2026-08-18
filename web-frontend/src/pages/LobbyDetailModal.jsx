import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { motion, useReducedMotion } from 'framer-motion';
import LobbyDetailContent from '../components/LobbyDetailContent.jsx';
import { IconX } from '../components/Icon.jsx';
import { springs, withReducedMotion } from '../motion/springs.js';

/**
 * A lobby-kartyabol nyilo reszletek-nezet. Ugyanazt a layoutId-t hasznalja, mint a
 * LobbyCard (lobby-card-{id}), igy a Framer Motion automatikusan "kimorfolja" a
 * kartyabol - nincs kulon kezzel irt kezdo/vegpozicio, a konyvtar szamolja ki a
 * kartya es a modal box-a kozotti kulonbseget es animalja at.
 * A hatterben a feed (backgroundLocation) tovabbra is fel van montirozva, ezert
 * mukodik a shared-element atmenet zarasnal (vissza a kartyara) is.
 */
export default function LobbyDetailModal() {
  const { id } = useParams();
  const navigate = useNavigate();
  const reduced = useReducedMotion();
  // Szinkron, animacio-fuggetlen zaro-jelzo: azonnal `false`-ra vált a backdrop
  // pointer-events-je, mielott meg barmilyen exit-animacio elindulna/lezarulna -
  // igy egy esetleg sose befejezodo animacio sem tudna lathatatlanul leblokkolni az oldalt.
  const [closing, setClosing] = useState(false);

  function close() {
    setClosing(true);
    // navigate(-1): egyet lep vissza a bongeszo-elozmenyben - mivel a modal ide egy
    // navigate(...) hivassal ERKEZETT (lasd LobbyCard.jsx / LobbiesPage.jsx), a "vissza"
    // pontosan a lista-nezetre visz, ahonnan indultunk.
    navigate(-1);
  }

  // Billentyuzet-vezerles: Escape-re is bezarhato a modal, nem csak a hatterre/X gombra
  // kattintva. Csak EGYSZER, mountoláskor iratkozik fel az esemenyre (lasd ures [] fuggosegi
  // listat), es a takaritas-fuggveny (return) leiratkoztat unmountoláskor.
  useEffect(() => {
    function onKeyDown(e) {
      if (e.key === 'Escape') close();
    }
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* Hatter-elsotetites - kattintasra bezarja a modalt. */}
      <motion.div
        className="sheet-backdrop"
        style={{ pointerEvents: closing ? 'none' : 'auto' }}
        initial={{ opacity: 0 }}
        animate={{ opacity: closing ? 0 : 1 }}
        exit={{ opacity: 0 }}
        transition={withReducedMotion(springs.structuralFast, reduced)}
        onClick={close}
      />
      <motion.div
        className="lobby-modal"
        style={{ pointerEvents: closing ? 'none' : 'auto' }}
        // EZ a legfontosabb sor: ugyanaz a layoutId string, mint a LobbyCard-on - a Framer
        // Motion ebbol ismeri fel, hogy ez a ket elem "ugyanaz", es automatikusan animalja
        // at a kartya meretebol/pozíciojabol a modal teljes meretere/pozíciojaba.
        layoutId={`lobby-card-${id}`}
        transition={withReducedMotion(springs.structural, reduced)}
      >
        <div className="lobby-modal-topbar">
          <button type="button" className="btn btn-ghost btn-sm" onClick={close}>
            <IconX size={14} /> Bezaras
          </button>
        </div>
        <div className="lobby-modal-content">
          {/* A tenyleges tartalom UGYANAZ a komponens, amit a teljes oldalas
              LobbyDetailPage is hasznal - itt csak a "keret" (modal/animacio) mas. */}
          <LobbyDetailContent id={id} />
        </div>
      </motion.div>
    </>
  );
}
