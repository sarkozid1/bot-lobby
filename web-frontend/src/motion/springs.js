// Kozponti spring-preszetek. Response/damping -> Framer Motion "duration/bounce" parameterezes,
// az apple-design guide altal ajanlott web-mappingnek megfeleloen.
//
// - structural: kritikusan csillapitott (damping 1.0), nincs tullendules - lista/lap/modal valtasokhoz.
// - momentum:   enyhe visszapattanas (damping ~0.8), CSAK gesztus-vezerelt (flick/drag-release) mozgashoz.
// - sheet:      bottom sheet nyitas/zaras - kicsit lazabb, mert huzhato/megszakithato.

export const springs = {
  structural: { type: 'spring', bounce: 0, duration: 0.4 },
  structuralFast: { type: 'spring', bounce: 0, duration: 0.25 },
  momentum: { type: 'spring', bounce: 0.2, duration: 0.4 },
  sheet: { type: 'spring', bounce: 0.18, duration: 0.35 },
  press: { type: 'spring', bounce: 0, duration: 0.15 }
};

// Rubberband gorbe hataron tuli huzashoz (Apple "Designing Fluid Interfaces" formula).
export function rubberband(overshoot, dimension, constant = 0.55) {
  return (overshoot * dimension * constant) / (dimension + constant * Math.abs(overshoot));
}

// Momentum-projekcio: hova erne ki a gesztus, ha hagynank kifutni (exponencialis decay, mint a scroll).
export function projectMomentum(velocity, decelerationRate = 0.998) {
  return (velocity / 1000) * decelerationRate / (1 - decelerationRate);
}

// Reduced-motion alatt minden spring egyszeru, nem-tullendulo cross-fade/mozgas legyen.
export function withReducedMotion(transition, reduced) {
  if (!reduced) return transition;
  return { type: 'tween', duration: 0.15, ease: 'easeOut' };
}
