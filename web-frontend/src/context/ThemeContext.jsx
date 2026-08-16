import { createContext, useCallback, useContext, useEffect, useState } from 'react';

const ThemeContext = createContext(null);
const STORAGE_KEY = 'botlobby-theme';
const ORDER = ['system', 'light', 'dark'];

function applyTheme(theme) {
  if (theme === 'system') {
    document.documentElement.removeAttribute('data-theme');
  } else {
    document.documentElement.setAttribute('data-theme', theme);
  }
}

// A tema erteket az index.html-be agyazott inline script mar alkalmazta betoltodeskor
// (FOUC elkerulesehez) - itt csak a React allapotot szinkronizaljuk vele.
export function ThemeProvider({ children }) {
  const [theme, setThemeState] = useState(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    return ORDER.includes(stored) ? stored : 'system';
  });

  useEffect(() => {
    applyTheme(theme);
    localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  const setTheme = useCallback((next) => setThemeState(next), []);
  const cycleTheme = useCallback(() => {
    setThemeState((prev) => ORDER[(ORDER.indexOf(prev) + 1) % ORDER.length]);
  }, []);

  return <ThemeContext.Provider value={{ theme, setTheme, cycleTheme }}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme csak ThemeProvider-en belul hasznalhato.');
  return ctx;
}
