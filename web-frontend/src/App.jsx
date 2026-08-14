import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';

import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import SettingsPage from './pages/SettingsPage.jsx';
import LobbiesPage from './pages/LobbiesPage.jsx';
import CreateLobbyPage from './pages/CreateLobbyPage.jsx';
import LobbyDetailPage from './pages/LobbyDetailPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';
import UserProfilePage from './pages/UserProfilePage.jsx';

export default function App() {
  const { token } = useAuth();

  return (
    <div className="app-shell">
      <Navbar />
      <main className="app-content">
        <Routes>
          <Route path="/" element={<Navigate to={token ? '/lobbies' : '/login'} replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/settings" element={<SettingsPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/lobbies" element={<LobbiesPage />} />
            <Route path="/lobbies/new" element={<CreateLobbyPage />} />
            <Route path="/lobbies/:id" element={<LobbyDetailPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/users/:id" element={<UserProfilePage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
