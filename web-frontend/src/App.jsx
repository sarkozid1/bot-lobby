import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { AnimatePresence } from 'framer-motion';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';

import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import SettingsPage from './pages/SettingsPage.jsx';
import LobbiesPage from './pages/LobbiesPage.jsx';
import LobbyDetailPage from './pages/LobbyDetailPage.jsx';
import LobbyDetailModal from './pages/LobbyDetailModal.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import UserProfilePage from './pages/UserProfilePage.jsx';

// A teljes app "vaza" - a Navbar mindig lathato, alatta valtakoznak az egyes oldalak a
// React Router <Routes>/<Route> parjaval. Ez a komponens dontia el, MELYIK route-ok
// vedettek (csak bejelentkezve erhetok el) es hogyan mukodik a kartyabol-nyilo modal minta.
export default function App() {
  const { token } = useAuth();
  const location = useLocation();

  // Modal-route minta: ha a kartyarol navigalunk ide, a location.state megorzi az
  // eredeti (feed) locationt, es a Routes AZT rendereli tovabbra is a hatterben -
  // a lobby reszletek egy kulon, felette lebego AnimatePresence rutinnak jelenik meg,
  // ami a kartyabol tud "kimorfolni" (lasd LobbyDetailModal, layoutId).
  const backgroundLocation = location.state?.backgroundLocation;

  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-content">
        {/* A FO Routes location propja: ha van backgroundLocation, EZT (a "regi", hatterben
            maradt URL-t) rendereli, NEM a tenyleges aktualis URL-t (ami mar /lobbies/{id}
            lenne) - igy pl. a lobby-lista komponens (LobbiesPage) MOUNTOLVA marad a
            hatterben, mikozben a cimsorban mar a lobby reszletek URL-je latszik. */}
        <Routes location={backgroundLocation || location}>
          {/* Gyoker URL: bejelentkezve a lobbylistara, egyebkent a login oldalra iranyit. */}
          <Route path="/" element={<Navigate to={token ? '/lobbies' : '/login'} replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/settings" element={<SettingsPage />} />

          {/* Ezek a route-ok a ProtectedRoute-on keresztul ERKEZNEK - ha nincs bejelentkezve
              a felhasznalo, a ProtectedRoute automatikusan a /login-ra iranyit, meg mielott
              a tenyleges oldalkomponens (LobbiesPage, stb.) egyaltalan renderelodne. */}
          <Route element={<ProtectedRoute />}>
            <Route path="/lobbies" element={<LobbiesPage />} />
            {/* Teljes oldalas lobby-reszletek - direkt URL-megnyitasra/frissitesre. */}
            <Route path="/lobbies/:id" element={<LobbyDetailPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/users/:id" element={<UserProfilePage />} />
          </Route>

          {/* Barmi mas (nem letezo utvonal) a gyokerre iranyit, ami aztan tovabb iranyit
              login/lobbies fele az entikentikaltsagtol fuggoen. */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

        {/* A modal-reteg: CSAK akkor renderelodik, ha VAN backgroundLocation (azaz kartyarol
            navigaltunk ide, nem kozvetlen URL-megnyitassal). Ez egy MASODIK, FUGGETLEN
            <Routes> peldany, ami a TENYLEGES aktualis URL-t nezi (nem a `location` prop-ot
            kapja meg, mint a fenti) - igy tudja megjeleniteni a LobbyDetailModal-t akkor is,
            amikor a fenti Routes meg mindig a regi (lista) nezetet mutatja a hatterben.
            AnimatePresence: lehetove teszi, hogy a modal bezarasakor egy exit-animacio
            lefusson, mielott a komponens tenylegesen kikerulne a DOM-bol. */}
        {backgroundLocation && (
          <AnimatePresence>
            <Routes>
              <Route element={<ProtectedRoute />}>
                <Route path="/lobbies/:id" element={<LobbyDetailModal />} />
              </Route>
            </Routes>
          </AnimatePresence>
        )}
      </main>
    </div>
  );
}
