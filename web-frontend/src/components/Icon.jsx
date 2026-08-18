// Egyseges, minimalista vonalikon-keszlet (24x24 grid, kerekitett vegek), hogy az
// emoji helyett konzisztens, terminal/OS-fuggetlen ikonografiat hasznaljon az app.

// Kozos "vazlat" komponens - minden ikon ezt hivja, csak a benne levo <path>/<circle>/<rect>
// vonalrajz kulonbozik. Igy minden ikon garantaltan ugyanazokkal a beallitasokkal
// (meret-alapertelmezes, vonalvastagsag, kerekitett vegek) rendelkezik, egy helyen valtoztathato.
function Svg({ size = 18, children, ...props }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      // fill="none" + stroke="currentColor": az ikonok NEM kitoltott alakzatok, hanem
      // vonalrajzok, es a szinuk a korulotte levo szoveg szinet oroklik (CSS `color`) - igy
      // automatikusan igazodnak a vilagos/sotet temahoz kulon beallitas nelkul.
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      // Dekoracios ikonok, nincs onallo jelentesuk a szoveg/cimke nelkul - kepernyoolvasok
      // szamara elrejtve (aria-hidden), es sose kapnak billentyuzet-fokuszt (focusable="false").
      aria-hidden="true"
      focusable="false"
      {...props}
    >
      {children}
    </svg>
  );
}

// Lobby-kartyakon a datum/ido mellett (lasd LobbyCard.jsx).
export function IconCalendar(props) {
  return (
    <Svg {...props}>
      <rect x="3" y="5" width="18" height="16" rx="3" />
      <path d="M8 3v4M16 3v4M3 10h18" />
    </Svg>
  );
}

// Lobby-kartyakon a letszam mellett.
export function IconUsers(props) {
  return (
    <Svg {...props}>
      <circle cx="9" cy="8" r="3.25" />
      <circle cx="17" cy="8.5" r="2.5" />
      <path d="M3.5 19c.7-3.2 3-4.9 5.5-4.9s4.8 1.7 5.5 4.9M15.7 14.4c2 .3 3.6 1.9 4.2 4.6" />
    </Svg>
  );
}

// (Jelenleg nincs hasznalva - korabban a most mar eltavolitott theme-toggle hasznalta.
// Itt hagyva, mert onallo, ujrafelhasznalhato ikon-eszkoz, hasonloan a tobbihez.)
export function IconSun(props) {
  return (
    <Svg {...props}>
      <circle cx="12" cy="12" r="4.25" />
      <path d="M12 2.5v2.3M12 19.2v2.3M4.6 4.6l1.6 1.6M17.8 17.8l1.6 1.6M2.5 12h2.3M19.2 12h2.3M4.6 19.4l1.6-1.6M17.8 6.2l1.6-1.6" />
    </Svg>
  );
}

export function IconMoon(props) {
  return (
    <Svg {...props}>
      <path d="M20 14.3A8.3 8.3 0 1 1 9.7 4a6.6 6.6 0 0 0 10.3 10.3Z" />
    </Svg>
  );
}

export function IconMonitor(props) {
  return (
    <Svg {...props}>
      <rect x="3" y="4.5" width="18" height="12" rx="2" />
      <path d="M8 20h8M12 16.5V20" />
    </Svg>
  );
}

// Bezaras-gombokon (pl. LobbyDetailModal "Bezaras" gombja).
export function IconX(props) {
  return (
    <Svg {...props}>
      <path d="M6 6l12 12M18 6L6 18" />
    </Svg>
  );
}

// (Jelenleg nincs kozvetlenul hasznalva - a swipe-sorban a "✓" szoveges glyph van hasznalva
// helyette, de ez keszen all, ha kesobb egysegesiteni kellene azt is ikonra.)
export function IconCheck(props) {
  return (
    <Svg {...props}>
      <path d="M5 12.5l4.5 4.5L19 7.5" />
    </Svg>
  );
}

// A teljes oldalas LobbyDetailPage "Vissza a lobbykhoz" linkjen.
export function IconChevronLeft(props) {
  return (
    <Svg {...props}>
      <path d="M15 5l-7 7 7 7" />
    </Svg>
  );
}

// A navbar "Beallitasok" (Settings) linkjen.
export function IconSettings(props) {
  return (
    <Svg {...props}>
      <circle cx="12" cy="12" r="3.2" />
      <path d="M12 3v2.4M12 18.6V21M21 12h-2.4M5.4 12H3M18.4 5.6l-1.7 1.7M7.3 16.7l-1.7 1.7M18.4 18.4l-1.7-1.7M7.3 7.3 5.6 5.6" />
    </Svg>
  );
}
