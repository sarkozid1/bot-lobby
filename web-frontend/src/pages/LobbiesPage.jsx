import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import LobbyCard from '../components/LobbyCard.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function LobbiesPage() {
  const [sports, setSports] = useState([]);
  const [filters, setFilters] = useState({ sport: '', city: '', date: '', skillLevel: '' });
  const [lobbies, setLobbies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.sports
      .list()
      .then(setSports)
      .catch(() => {});
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await api.lobbies.search(filters);
      setLobbies(result);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  useEffect(() => {
    load();
  }, [load]);

  function updateFilter(field, value) {
    setFilters((prev) => ({ ...prev, [field]: value }));
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Lobbyk</h1>
        <Link to="/lobbies/new" className="btn btn-primary">
          + Uj lobby
        </Link>
      </div>

      <form
        className="filter-bar card"
        onSubmit={(e) => {
          e.preventDefault();
          load();
        }}
      >
        <select value={filters.sport} onChange={(e) => updateFilter('sport', e.target.value)}>
          <option value="">Minden sport</option>
          {sports.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
        <input placeholder="Varos" value={filters.city} onChange={(e) => updateFilter('city', e.target.value)} />
        <input type="date" value={filters.date} onChange={(e) => updateFilter('date', e.target.value)} />
        <select value={filters.skillLevel} onChange={(e) => updateFilter('skillLevel', e.target.value)}>
          <option value="">Minden szint</option>
          <option value="BEGINNER">Kezdo</option>
          <option value="INTERMEDIATE">Halado</option>
          <option value="ADVANCED">Fejlett</option>
          <option value="PRO">Profi</option>
        </select>
        <button className="btn btn-secondary" type="submit">
          Szures
        </button>
      </form>

      <ErrorBanner error={error} />

      {loading ? (
        <div className="page-loading">Betoltes...</div>
      ) : lobbies.length === 0 ? (
        <p className="empty-state">Nincs a szuroknek megfelelo lobby. Hozz letre egyet!</p>
      ) : (
        <div className="lobby-grid">
          {lobbies.map((l) => (
            <LobbyCard key={l.id} lobby={l} />
          ))}
        </div>
      )}
    </div>
  );
}
