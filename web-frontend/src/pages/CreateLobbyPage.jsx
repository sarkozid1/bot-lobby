import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useToast } from '../context/ToastContext.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function CreateLobbyPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [sports, setSports] = useState([]);
  const [form, setForm] = useState({
    sportId: '',
    title: '',
    description: '',
    city: '',
    startTime: '',
    maxParticipants: 4,
    minSkillLevel: ''
  });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.sports
      .list()
      .then((list) => {
        setSports(list);
        if (list.length > 0) setForm((prev) => ({ ...prev, sportId: list[0].id }));
      })
      .catch(() => {});
  }, []);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const created = await api.lobbies.create({
        sportId: form.sportId,
        title: form.title,
        description: form.description || null,
        city: form.city,
        startTime: form.startTime,
        maxParticipants: Number(form.maxParticipants),
        minSkillLevel: form.minSkillLevel || null
      });
      toast.success('Lobby letrehozva!');
      navigate(`/lobbies/${created.id}`);
    } catch (err) {
      setError(err);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page page-narrow">
      <h1>Uj lobby letrehozasa</h1>
      <form className="card form-card" onSubmit={handleSubmit}>
        <ErrorBanner error={error} />

        <label>
          Sport
          <select required value={form.sportId} onChange={(e) => update('sportId', e.target.value)}>
            {sports.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Cim
          <input
            required
            value={form.title}
            onChange={(e) => update('title', e.target.value)}
            placeholder="Focizas pentek este"
          />
        </label>

        <label>
          Leiras
          <textarea
            value={form.description}
            onChange={(e) => update('description', e.target.value)}
            placeholder="Negyen vagyunk, kell meg ketto."
          />
        </label>

        <div className="form-row">
          <label>
            Varos
            <input required value={form.city} onChange={(e) => update('city', e.target.value)} placeholder="Budapest" />
          </label>
          <label>
            Kezdes
            <input
              type="datetime-local"
              required
              value={form.startTime}
              onChange={(e) => update('startTime', e.target.value)}
            />
          </label>
        </div>

        <div className="form-row">
          <label>
            Max letszam
            <input
              type="number"
              min={1}
              required
              value={form.maxParticipants}
              onChange={(e) => update('maxParticipants', e.target.value)}
            />
          </label>
          <label>
            Min skill szint
            <select value={form.minSkillLevel} onChange={(e) => update('minSkillLevel', e.target.value)}>
              <option value="">(nincs megkotes)</option>
              <option value="BEGINNER">Kezdo</option>
              <option value="INTERMEDIATE">Halado</option>
              <option value="ADVANCED">Fejlett</option>
              <option value="PRO">Profi</option>
            </select>
          </label>
        </div>

        <button className="btn btn-primary btn-block" type="submit" disabled={submitting}>
          {submitting ? 'Letrehozas...' : 'Lobby letrehozasa'}
        </button>
      </form>
    </div>
  );
}
