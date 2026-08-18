// Kozponti spring-preszetek. Response/damping -> Framer Motion "duration/bounce" parameterezes,
// az apple-design guide altal ajanlott web-mappingnek megfeleloen.
//
// - structural: kritikusan csillapitott (damping 1.0), nincs tullendules - lista/lap/modal valtasokhoz.
// - momentum:   enyhe visszapattanas (damping ~0.8), CSAK gesztus-vezerelt (flick/drag-release) mozgashoz.
// - sheet:      bottom sheet nyitas/zaras - kicsit lazabb, mert huzhato/megszakithato.
//
// A "bounce" (0-1) azt szabalyozza, mennyire lengjen tul a spring a celertek elerese elott
// (0 = egyaltalan nem), a "duration" pedig kb. mennyi ido alatt all meg - ez a ket ertek
// egyutt hatarozza meg a spring "feszesseget/erzetet", anelkul, hogy nyers fizikai
// parametereket (stiffness/damping/mass) kellene kezzel hangolni.
export const springs = {
  structural: { type: 'spring', bounce: 0, duration: 0.4 },
  structuralFast: { type: 'spring', bounce: 0, duration: 0.25 },
  momentum: { type: 'spring', bounce: 0.2, duration: 0.4 },
  sheet: { type: 'spring', bounce: 0.18, duration: 0.35 },
  press: { type: 'spring', bounce: 0, duration: 0.15 }
};

// Rubberband gorbe hataron tuli huzashoz (Apple "Designing Fluid Interfaces" formula).
// Minel tavolabb huzzak a hataron tul (overshoot), annal jobban "ellenall" a mozgas - a
// visszaadott ertek sosem eri el a "dimension" erteket, akarmekkora is az overshoot, igy
// erzi ugy a felhasznalo, hogy egy rugalmas anyagot huz, nem egy merev hatart tol at.
// (Jelenleg nincs kozvetlenul hasznalva - a BottomSheet a Framer Motion sajat
// dragElastic-jet hasznalja ugyanerre a celra - de itt all keszen, ha kezi vezerlesre lenne szukseg.)
export function rubberband(overshoot, dimension, constant = 0.55) {
  return (overshoot * dimension * constant) / (dimension + constant * Math.abs(overshoot));
}

// Momentum-projekcio: hova erne ki a gesztus, ha hagynank kifutni (exponencialis decay, mint a scroll).
// A velocity (px/s) alapjan kiszamolja a "vegso" elmozdulast, ha a mozgas termeszetesen,
// surlodassal lassulna le - hasonlo ahhoz, ahogy egy telefon scroll-listaja "kifut", amikor
// elengeded lenditeskor. (Jelenleg szinten nincs kozvetlenul hasznalva, elokeszitett eszkoz.)
export function projectMomentum(velocity, decelerationRate = 0.998) {
  return (velocity / 1000) * decelerationRate / (1 - decelerationRate);
}

// Reduced-motion alatt minden spring egyszeru, nem-tullendulo cross-fade/mozgas legyen.
// Minden animalt komponens (MotionButton, BottomSheet, Switch, stb.) a sajat
// springs.xxx-jet EZEN a fuggvenyen vezeti at a transition propba adas elott - ha a
// felhasznalo rendszerszinten "Reduce Motion"-t kert (useReducedMotion() -> true), minden
// spring helyett egy rovid, egyenletes (nem-fizikai) tween-re valt, hogy tavolsagcsokkento/
// mozgasbeteg felhasznaloknal se legyenek zavaro, lengo animaciok.
export function withReducedMotion(transition, reduced) {
  if (!reduced) return transition;
  return { type: 'tween', duration: 0.15, ease: 'easeOut' };
}
