import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

// A "/login" route tartalma - lasd App.jsx-ben, ez a KEVES nem-vedett (ProtectedRoute nelkuli) oldal egyike.
export default function LoginPage() {
  const { applyAuth } = useAuth();
  const navigate = useNavigate();
  // Ha a felhasznalot egy vedett oldalrol (pl. mert lejart a tokene) iranyitottak ide, a
  // location.state.from megorzi, HOVA kell visszakuldeni sikeres bejelentkezes utan.
  const location = useLocation();
  const toast = useToast();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    // A natic bongeszo-urlapelkuldes megakadalyozasa (ami teljes oldal-ujratoltest okozna) -
    // helyette JavaScript-bol, fetch-csel kuldjuk el az adatokat.
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const result = await api.auth.login({ email, password });
      // Elmenti a kapott JWT-t es betolti a teljes profilt (lasd AuthContext.applyAuth).
      await applyAuth(result);
      toast.success(`Szia, ${result.name}!`);
      // replace: true - a bongeszo elozmenyeben a login oldal NEM marad benne, igy a
      // "vissza" gomb nem viszi vissza ide bejelentkezes utan.
      navigate(location.state?.from || '/lobbies', { replace: true });
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Bejelentkezes</h1>
        <ErrorBanner error={error} />

        <label>
          Email
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>

        <label>
          Jelszo
          <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>

        <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
          {submitting ? 'Bejelentkezes...' : 'Bejelentkezes'}
        </button>

        <p className="auth-switch">
          Nincs meg fiokod? <Link to="/register">Regisztralj</Link>
        </p>
      </form>
    </div>
  );
}
