import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { IconSettings } from './Icon.jsx';

// A teljes app egyetlen, mindig lathato felso navigacios sav-ja (lasd App.jsx-ben, ahol az
// osszes route felett all, nem resze a Routes-nak).
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

        {/* A kozepso navigacios linkek csak bejelentkezve latszanak - kijelentkezett
            allapotban nincs ertelme "Lobbyk"/"Profilom" linkeket mutatni. */}
        {token && (
          <nav className="navbar-links">
            {/* NavLink (nem sima Link): a callback-fuggveny formaju className automatikusan
                hozzaadja az "active" osztalyt, ha epp ez a link egyezik az aktualis URL-lel -
                igy latszik, melyik oldalon vagyunk (lasd styles/index.css .navbar-links a.active). */}
            <NavLink to="/lobbies" className={({ isActive }) => (isActive ? 'active' : '')}>
              Lobbyk
            </NavLink>
            <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
              Profilom
            </NavLink>
          </nav>
        )}

        <div className="navbar-user">
          <NavLink to="/settings" className="navbar-icon-link" title="Backend URL beallitasa" aria-label="Beallitasok">
            <IconSettings size={17} />
          </NavLink>
          {/* Ket teljesen kulonbozo allapot: bejelentkezve (nev + kijelentkezes gomb) vagy
              kijelentkezve (bejelentkezes/regisztracio gombok). */}
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
