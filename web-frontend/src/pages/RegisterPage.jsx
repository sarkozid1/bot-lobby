import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function RegisterPage() {
  const { applyAuth } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const [form, setForm] = useState({ name: '', email: '', password: '', city: '' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const result = await api.auth.register({
        name: form.name,
        email: form.email,
        password: form.password,
        city: form.city || null
      });
      await applyAuth(result);
      toast.success('Sikeres regisztracio!');
      navigate('/lobbies', { replace: true });
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSubmit}>
        <h1>Regisztracio</h1>
        <ErrorBanner error={error} />

        <label>
          Nev
          <input required value={form.name} onChange={(e) => update('name', e.target.value)} />
        </label>

        <label>
          Email
          <input type="email" required value={form.email} onChange={(e) => update('email', e.target.value)} />
        </label>

        <label>
          Jelszo (min. 8 karakter)
          <input
            type="password"
            required
            minLength={8}
            value={form.password}
            onChange={(e) => update('password', e.target.value)}
          />
        </label>

        <label>
          Varos (opcionalis)
          <input value={form.city} onChange={(e) => update('city', e.target.value)} placeholder="Budapest" />
        </label>

        <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
          {submitting ? 'Regisztracio...' : 'Regisztracio'}
        </button>

        <p className="auth-switch">
          Mar van fiokod? <Link to="/login">Jelentkezz be</Link>
        </p>
      </form>
    </div>
  );
}
