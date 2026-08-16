import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client.js';
import LobbyCard from '../components/LobbyCard.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';
import BottomSheet from '../motion/BottomSheet.jsx';
import MotionButton from '../motion/MotionButton.jsx';

const EMPTY_FILTERS = { sport: '', city: '', date: '', skillLevel: '' };

export default function LobbiesPage() {
  const [sports, setSports] = useState([]);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [draftFilters, setDraftFilters] = useState(EMPTY_FILTERS);
  const [sheetOpen, setSheetOpen] = useState(false);
  const [lobbies, setLobbies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.sports
      .list()
      .then(setSports)
      .catch(() => {});
  }, []);

  const load = useCallback(async (activeFilters) => {
    setLoading(true);
    setError(null);
    try {
      const result = await api.lobbies.search(activeFilters);
      setLobbies(result);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load(filters);
  }, [filters, load]);

  function openSheet() {
    setDraftFilters(filters);
    setSheetOpen(true);
  }

  function applyFilters(e) {
    e.preventDefault();
    setFilters(draftFilters);
    setSheetOpen(false);
  }

  function clearFilters() {
    setDraftFilters(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
    setSheetOpen(false);
  }

  const activeFilterCount = Object.values(filters).filter(Boolean).length;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Lobbyk</h1>
        <div className="page-header-actions">
          <MotionButton className="btn-secondary" onClick={openSheet}>
            Szurok{activeFilterCount > 0 ? ` (${activeFilterCount})` : ''}
          </MotionButton>
          <Link to="/lobbies/new" className="btn btn-primary">
            + Uj lobby
          </Link>
        </div>
      </div>

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

      <BottomSheet open={sheetOpen} onClose={() => setSheetOpen(false)} title="Szures">
        <form className="filter-sheet-form" onSubmit={applyFilters}>
          <label>
            Sport
            <select
              value={draftFilters.sport}
              onChange={(e) => setDraftFilters((p) => ({ ...p, sport: e.target.value }))}
            >
              <option value="">Minden sport</option>
              {sports.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            Varos
            <input
              value={draftFilters.city}
              onChange={(e) => setDraftFilters((p) => ({ ...p, city: e.target.value }))}
              placeholder="Budapest"
            />
          </label>

          <label>
            Datum
            <input
              type="date"
              value={draftFilters.date}
              onChange={(e) => setDraftFilters((p) => ({ ...p, date: e.target.value }))}
            />
          </label>

          <label>
            Skill szint
            <select
              value={draftFilters.skillLevel}
              onChange={(e) => setDraftFilters((p) => ({ ...p, skillLevel: e.target.value }))}
            >
              <option value="">Minden szint</option>
              <option value="BEGINNER">Kezdo</option>
              <option value="INTERMEDIATE">Halado</option>
              <option value="ADVANCED">Fejlett</option>
              <option value="PRO">Profi</option>
            </select>
          </label>

          <div className="sheet-actions">
            <MotionButton type="button" className="btn-ghost" onClick={clearFilters}>
              Torles
            </MotionButton>
            <MotionButton type="submit" className="btn-primary">
              Szurok alkalmazasa
            </MotionButton>
          </div>
        </form>
      </BottomSheet>
    </div>
  );
}
