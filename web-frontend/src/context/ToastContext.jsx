import { createContext, useCallback, useContext, useRef, useState } from 'react';

// Lasd AuthContext.jsx a React Context altalanos mintajaert.
// Ez a "toast" (rovid, automatikusan eltuno ertesites) rendszer globalis allapota - barmelyik
// komponens tud ertesitest mutatni a useToast() hook-on keresztul, anelkul, hogy tudnia
// kellene, HOL/HOGYAN jelenik meg vizualisan (azt ez a Provider fajl kezeli egy helyen).
const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  // useRef, nem useState: a szamlalonak NEM kell ujra-renderelest kivaltania, amikor
  // novekszik - csak egy egyedi azonositot kell generalnia minden uj toast-hoz.
  const idRef = useRef(0);

  const remove = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  // Uj toast hozzaadasa a listahoz, es automatikus eltavolitas 4.5 masodperc mulva
  // (setTimeout) - a felhasznalonak nem kell kezzel bezarnia oket (bar kattintasra is
  // eltunnek, lasd a lenti JSX onClick-jet).
  const push = useCallback(
    (message, type = 'info') => {
      const id = ++idRef.current;
      setToasts((prev) => [...prev, { id, message, type }]);
      setTimeout(() => remove(id), 4500);
    },
    [remove]
  );

  // A hivo oldalon (pl. toast.success('Lobby letrehozva!')) ez a harom fuggveny erheto el -
  // mindegyik csak a push(...) egy-egy elore beallitott "type" parameterrel torteno hivasa.
  const toast = {
    success: (msg) => push(msg, 'success'),
    error: (msg) => push(msg, 'error'),
    info: (msg) => push(msg, 'info')
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      {/* A toast-ok vizualis megjelenitese KOZPONTOSITVA, egyetlen, fixen pozicionalt
          konteinerben tortenik - igy barhonnan hivodik is a toast.success(...), a
          megjelenites mindig ugyanott (a kepernyo sarkaban) es ugyanugy nez ki. */}
      <div className="toast-stack">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type}`} onClick={() => remove(t.id)}>
            {t.message}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast csak ToastProvider-en belul hasznalhato.');
  return ctx;
}
