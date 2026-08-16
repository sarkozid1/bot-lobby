import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { IconSettings } from './Icon.jsx';
import ThemeToggle from './ThemeToggle.jsx';

export default function Navbar() {
  const { user, token, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <NavLink to="/lobbies" className="navbar-brand">
          <span className="navbar-brand-dot" />
          BotLobby
        </NavLink>

        {token && (
          <nav className="navbar-links">
            <NavLink to="/lobbies" className={({ isActive }) => (isActive ? 'active' : '')}>
              Lobbyk
            </NavLink>
            <NavLink to="/lobbies/new" className={({ isActive }) => (isActive ? 'active' : '')}>
              Uj lobby
            </NavLink>
            <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
              Profilom
            </NavLink>
          </nav>
        )}

        <div className="navbar-user">
          <ThemeToggle />
          <NavLink to="/settings" className="navbar-icon-link" title="Backend URL beallitasa" aria-label="Beallitasok">
            <IconSettings size={17} />
          </NavLink>
          {token ? (
            <>
              <span className="navbar-username">{user?.name || '...'}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
                Kijelentkezes
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="btn btn-ghost btn-sm">
                Bejelentkezes
              </NavLink>
              <NavLink to="/register" className="btn btn-primary btn-sm">
                Regisztracio
              </NavLink>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
