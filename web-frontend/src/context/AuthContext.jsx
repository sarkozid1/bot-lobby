import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, getToken, setToken as persistToken } from '../api/client';

// React Context: egy globalis, "mindenhonnan elerheto" allapot-tarolo, hogy ne kelljen a
// bejelentkezett felhasznalo adatait (token, user) kezzel vegigadni komponens-prop-kent az
// egesz komponensfan (pl. App -> Navbar -> ... -> egy mely gomb). Minden komponens, ami
// useAuth()-ot hiv, automatikusan ujra-renderelodik, ha ez az allapot valtozik.
const AuthContext = createContext(null);

// Ezt a Provider-t egyszer, legkivul csomagolja korul main.jsx - minden ala agyazott
// komponens (gyakorlatilag az egesz App) el tudja erni a value-ban megadott adatokat/fuggvenyeket.
export function AuthProvider({ children }) {
  // A kezdeti allapot a localStorage-bol jon (getToken()) - igy oldal-ujratoltes utan is
  // "emlekszik" a bejelentkezesre, nem kell ujra bejelentkezni minden reload-nal.
  const [token, setTokenState] = useState(getToken());
  const [user, setUser] = useState(null);
  // Kezdetben csak akkor "toltunk" (loading=true), ha VAN mentett token - ilyenkor meg
  // ellenorizni kell, hogy ervenyes-e (lasd lenti useEffect). Token nelkul nincs mit
  // ellenorizni, azonnal keszen allunk.
  const [loading, setLoading] = useState(Boolean(getToken()));

  // useCallback: a fuggveny referenciaja stabil marad ujrarenderelesek kozott (csak akkor
  // jon letre uj peldany, ha a fuggosegi lista - itt [] - valtozik) - ez fontos, mert ezek a
  // fuggvenyek bekerulnek a Context value-jaba, es ha minden renderkor uj referenciat kapnanak,
  // FELESLEGESEN ujra-renderelodne minden useAuth()-ot hasznalo komponens.
  const logout = useCallback(() => {
    persistToken(null);
    setTokenState(null);
    setUser(null);
  }, []);

  // Ujra lekeri a "sajat profilomat" a backendtol (GET /api/users/me) - ezt hivja mind a
  // kezdeti betoltes (lasd useEffect lent), mind sikeres login/regisztracio utan.
  const refreshMe = useCallback(async () => {
    const me = await api.users.me();
    setUser(me);
    return me;
  }, []);

  // Sikeres regisztracio/bejelentkezes utan hivodik (lasd LoginPage.jsx, RegisterPage.jsx) -
  // elmenti a kapott JWT-t, es betolti a teljes profilt.
  const applyAuth = useCallback(
    async (authResponse) => {
      persistToken(authResponse.accessToken);
      setTokenState(authResponse.accessToken);
      // A regisztracio/login valasz csak alap adatokat ad vissza - a teljes profilt (varos, sportok stb.) kulon toltjuk be.
      setUser({ id: authResponse.userId, name: authResponse.name, email: authResponse.email });
      try {
        await refreshMe();
      } catch {
        // nem vegzetes, a minimal user infoval is mukodik az app
      }
    },
    [refreshMe]
  );

  // Csak EGYSZER, komponens-mountoláskor fut le (lasd az ures [] fuggosegi listat) - ha van
  // mentett token, ellenorzi, hogy meg ervenyes-e (a backend elutasitana, ha lejart volna),
  // es ha nem, automatikusan kijelentkeztet.
  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }
    refreshMe()
      .catch(() => logout())
      .finally(() => setLoading(false));
    // csak induláskor fusson le
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Minden ala agyazott komponens ezt az objektumot kapja meg a useAuth() hivasan keresztul.
  return (
    <AuthContext.Provider value={{ token, user, loading, applyAuth, logout, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

// Kenyelmi "hook": ahelyett, hogy minden komponensben useContext(AuthContext)-et kellene
// irni, egyszeruen useAuth()-ot hivunk. A hibauzenet segit felismerni, ha valaki veletlenul
// az AuthProvider-en KIVUL probalja hasznalni.
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth csak AuthProvider-en belul hasznalhato.');
  return ctx;
}
