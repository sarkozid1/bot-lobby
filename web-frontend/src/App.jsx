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
        <Routes location={backgroundLocation || location}>
          <Route path="/" element={<Navigate to={token ? '/lobbies' : '/login'} replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/settings" element={<SettingsPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/lobbies" element={<LobbiesPage />} />
            <Route path="/lobbies/:id" element={<LobbyDetailPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/users/:id" element={<UserProfilePage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>

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
