import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

// React Router "route guard" minta: ez a komponens maga sose renderel tenyleges oldal-
// tartalmat, csak ELDONTI, mi tortenjen. Lasd App.jsx-ben: az <Outlet/> helyet foglal a
// vedett oldalaknak (/lobbies, /profile, stb.), amiket <Route element={<ProtectedRoute/>}>
// gyerekekent regisztralunk - igy egyetlen helyen van a "csak bejelentkezve" szabaly, nem
// kell minden vedett oldalba kulon-kulon beleirni.
export default function ProtectedRoute() {
  const { token, loading } = useAuth();

  // Amig az AuthContext meg ellenorzi a mentett tokent (lasd AuthContext.jsx useEffect-je),
  // ne dontsunk meg semmit - kulonben egy pillanatra a login oldalra villanhatna at valaki,
  // aki valojaban be van jelentkezve, csak meg nem toltodott be a valasz.
  if (loading) return <div className="page-loading">Betoltes...</div>;
  // Nincs ervenyes token -> a bejelentkezes oldalra iranyitunk. replace: a bongeszo
  // "vissza" gombja nem viszi vissza a felhasznalot a vedett (es ugyis elutasitott) oldalra.
  if (!token) return <Navigate to="/login" replace />;

  // Van ervenyes token -> az <Outlet/> rendereli a tenyleges egyezo gyerek route-ot
  // (pl. LobbiesPage, ProfilePage) a helyere.
  return <Outlet />;
}
