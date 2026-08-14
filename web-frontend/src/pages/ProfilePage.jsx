import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { RatingStarsDisplay } from '../components/RatingStars.jsx';
import { SkillBadge } from '../components/Badge.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function ProfilePage() {
  const { user, refreshMe } = useAuth();
  const toast = useToast();
  const [sports, setSports] = useState([]);
  const [form, setForm] = useState({ name: '', city: '', profilePictureUrl: '' });
  const [sportForm, setSportForm] = useState({ sportId: '', skillLevel: 'BEGINNER' });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (user) {
      setForm({
        name: user.name || '',
        city: user.city || '',
        profilePictureUrl: user.profilePictureUrl || ''
      });
    }
  }, [user]);

  useEffect(() => {
    api.sports
      .list()
      .then((list) => {
        setSports(list);
        if (list.length > 0) setSportForm((prev) => ({ ...prev, sportId: list[0].id }));
      })
      .catch(() => {});
  }, []);

  async function handleProfileSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.users.updateMe({
        name: form.name,
        city: form.city || null,
        profilePictureUrl: form.profilePictureUrl || null
      });
      await refreshMe();
      toast.success('Profil frissitve.');
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleSportSubmit(e) {
    e.preventDefault();
    try {
      await api.users.upsertSportProfile(sportForm);
      await refreshMe();
      toast.success('Sport-profil mentve.');
    } catch (err) {
      toast.error(err.message);
    }
  }

  if (!user) return <div className="page-loading">Betoltes...</div>;

  return (
    <div className="page page-narrow">
      <h1>Profilom</h1>

      <div className="card profile-summary">
        <div>
          <h2>{user.name}</h2>
          <p className="muted">{user.email}</p>
        </div>
        <div className="profile-reliability">
          <RatingStarsDisplay value={user.reliabilityScore} />
          <span>{Number(user.reliabilityScore ?? 0).toFixed(2)} megbizhatosag</span>
        </div>
      </div>

      <form className="card form-card" onSubmit={handleProfileSubmit}>
        <h2>Adatok szerkesztese</h2>
        <ErrorBanner error={error} />

        <label>
          Nev
          <input required value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} />
        </label>

        <label>
          Varos
          <input value={form.city} onChange={(e) => setForm((p) => ({ ...p, city: e.target.value }))} />
        </label>

        <label>
          Profilkep URL
          <input
            value={form.profilePictureUrl}
            onChange={(e) => setForm((p) => ({ ...p, profilePictureUrl: e.target.value }))}
          />
        </label>

        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? 'Mentes...' : 'Mentes'}
        </button>
      </form>

      <div className="card">
        <h2>Sportprofiljaim</h2>
        {user.sportProfiles?.length > 0 ? (
          <ul className="sport-profile-list">
            {user.sportProfiles.map((sp) => (
              <li key={sp.id}>
                {sp.sportName} <SkillBadge level={sp.skillLevel} />
              </li>
            ))}
          </ul>
        ) : (
          <p className="empty-state">Meg nincs felvett sportprofilod.</p>
        )}

        <form className="form-row" onSubmit={handleSportSubmit}>
          <select
            value={sportForm.sportId}
            onChange={(e) => setSportForm((p) => ({ ...p, sportId: e.target.value }))}
          >
            {sports.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <select
            value={sportForm.skillLevel}
            onChange={(e) => setSportForm((p) => ({ ...p, skillLevel: e.target.value }))}
          >
            <option value="BEGINNER">Kezdo</option>
            <option value="INTERMEDIATE">Halado</option>
            <option value="ADVANCED">Fejlett</option>
            <option value="PRO">Profi</option>
          </select>
          <button className="btn btn-secondary" type="submit">
            Hozzaad / frissit
          </button>
        </form>
      </div>
    </div>
  );
}
