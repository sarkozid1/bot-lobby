import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, getToken, setToken as persistToken } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(getToken());
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(getToken()));

  const logout = useCallback(() => {
    persistToken(null);
    setTokenState(null);
    setUser(null);
  }, []);

  const refreshMe = useCallback(async () => {
    const me = await api.users.me();
    setUser(me);
    return me;
  }, []);

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

  return (
    <AuthContext.Provider value={{ token, user, loading, applyAuth, logout, refreshMe }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth csak AuthProvider-en belul hasznalhato.');
  return ctx;
}
