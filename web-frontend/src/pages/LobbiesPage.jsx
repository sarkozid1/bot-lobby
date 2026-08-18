import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/client.js';
import { useToast } from '../context/ToastContext.jsx';
import LobbyCard from '../components/LobbyCard.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';
import BottomSheet from '../motion/BottomSheet.jsx';
import MotionButton from '../motion/MotionButton.jsx';

// "Ures" alapertelmezett ertekek - a szurok visszaallitasakor (Torles gomb) es a kezdeti
// allapotnal is ide ter vissza az urlap.
const EMPTY_FILTERS = { sport: '', city: '', date: '', skillLevel: '' };
const EMPTY_LOBBY_FORM = {
  sportId: '',
  title: '',
  description: '',
  city: '',
  startTime: '',
  maxParticipants: 4,
  minSkillLevel: ''
};

// A "/lobbies" fooldal: a lobby-lista/racs, a szuro-urlap, ES az "Uj lobby" letrehozo-urlap
// is EBBEN a komponensben el (mindketto egy-egy BottomSheet-ben, lasd lent) - igy a
// letrehozas nem kulon oldalra navigal, hanem felugro urlapkent jelenik meg a lista felett.
export default function LobbiesPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();

  const [sports, setSports] = useState([]);
  // "filters" a TENYLEGESEN alkalmazott szures (ez alapjan tortenik a lekerdezes), mig
  // "draftFilters" a szuro-urlapon epp SZERKESZTES ALATT levo, meg nem alkalmazott ertekek -
  // ezt a ketto elvalasztast az teszi szuksegesse, hogy a felhasznalo module-szeruen
  // atirogathassa a szurofeltetelt anelkul, hogy minden billentyuleutesnel ujra lekerdezne a listat.
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [draftFilters, setDraftFilters] = useState(EMPTY_FILTERS);
  const [sheetOpen, setSheetOpen] = useState(false);
  const [lobbies, setLobbies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Kulon allapot-keszlet az "Uj lobby" letrehozo urlaphoz (fuggetlen a szuro-urlaptol).
  const [createOpen, setCreateOpen] = useState(false);
  const [createForm, setCreateForm] = useState(EMPTY_LOBBY_FORM);
  const [createError, setCreateError] = useState(null);
  const [creating, setCreating] = useState(false);

  // A sportok listaja KETSZER hasznalt ezen az oldalon: a szuro-urlap "Sport" legordulojeben
  // ES az "Uj lobby" urlap "Sport" legordulojeben is - igy eleg egyszer lekerdezni, mindket
  // BottomSheet ugyanazt a `sports` allapotot olvassa.
  useEffect(() => {
    api.sports
      .list()
      .then(setSports)
      .catch(() => {});
  }, []);

  // useCallback: stabil referencia, hogy a lenti useEffect fuggosegi listajaban ne okozzon
  // vegtelen ujratoltest.
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

  // Minden alkalommal ujratolti a listat, amikor a TENYLEGESEN alkalmazott `filters`
  // megvaltozik (nem minden draftFilters-billentyuleutesnel, csak amikor a felhasznalo
  // ráklikkel a "Szurok alkalmazasa" gombra, lasd applyFilters lent).
  useEffect(() => {
    load(filters);
  }, [filters, load]);

  function openSheet() {
    // A draft mindig a jelenleg ERVENYBEN levo szurokrol indul (nem az ureskent) - ha
    // ujranyitjak a szuro-sheet-et, a mar beallitott ertekeket latjak, nem egy ures urlapot.
    setDraftFilters(filters);
    setSheetOpen(true);
  }

  function applyFilters(e) {
    e.preventDefault();
    // A draft ertekek innentol "elesbe mennek" - ez valtja ki a fenti useEffect-et, ami
    // ujratolti a listat.
    setFilters(draftFilters);
    setSheetOpen(false);
  }

  function clearFilters() {
    setDraftFilters(EMPTY_FILTERS);
    setFilters(EMPTY_FILTERS);
    setSheetOpen(false);
  }

  function openCreateSheet() {
    // A sport legordulo alapertelmezetten az ELSO betoltott sportra all - ha meg nem toltodott
    // be egy sport se (sports meg ures), a `sports[0]?.id ?? ''` biztonsagosan ures stringre esik vissza.
    setCreateForm({ ...EMPTY_LOBBY_FORM, sportId: sports[0]?.id ?? '' });
    setCreateError(null);
    setCreateOpen(true);
  }

  function updateCreateForm(field, value) {
    setCreateForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleCreateSubmit(e) {
    e.preventDefault();
    setCreateError(null);
    setCreating(true);
    try {
      const created = await api.lobbies.create({
        sportId: createForm.sportId,
        title: createForm.title,
        description: createForm.description || null,
        city: createForm.city,
        startTime: createForm.startTime,
        // A <input type="number"> mindig String-et ad vissza - Number(...)-rel alakitjuk
        // szammá, mielott a backendnek kuldenenk (ami Integer-t var, lasd LobbyCreateRequest).
        maxParticipants: Number(createForm.maxParticipants),
        minSkillLevel: createForm.minSkillLevel || null
      });
      setCreateOpen(false);
      toast.success('Lobby letrehozva!');
      // Ujratoltjuk a listat is (nem csak lokalisan fuzzuk hozza az uj lobbyt) - igy amikor
      // a felhasznalo bezarja a lent nyilo reszletek-modalt, a friss lobby mar ott van a listaban.
      load(filters);
      // Ugyanaz a modal-route minta, mint kartyara kattintaskor (lasd LobbyCard) - a lista
      // a hatterben marad, a friss lobby reszletei felugro modalkent jelennek meg felette.
      navigate(`/lobbies/${created.id}`, { state: { backgroundLocation: location } });
    } catch (err) {
      setCreateError(err);
    } finally {
      setCreating(false);
    }
  }

  // Hany szurofeltetel van eppen aktivan beallitva - ez jelenik meg a "Szurok (N)" gomb
  // feliraton, hogy a felhasznalo lassa, van-e egyaltalan aktiv szures.
  const activeFilterCount = Object.values(filters).filter(Boolean).length;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Lobbyk</h1>
        <div className="page-header-actions">
          <MotionButton className="btn-secondary" onClick={openSheet}>
            Szurok{activeFilterCount > 0 ? ` (${activeFilterCount})` : ''}
          </MotionButton>
          <MotionButton className="btn-primary" onClick={openCreateSheet}>
            + Uj lobby
          </MotionButton>
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

      {/* Szuro-urlap felugro sheet-ben. */}
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

      {/* "Uj lobby" letrehozo urlap - szinten felugro sheet-ben, NEM kulon oldalon. */}
      <BottomSheet open={createOpen} onClose={() => setCreateOpen(false)} title="Uj lobby">
        <form className="create-lobby-sheet-form" onSubmit={handleCreateSubmit}>
          <ErrorBanner error={createError} />

          <label>
            Sport
            <select required value={createForm.sportId} onChange={(e) => updateCreateForm('sportId', e.target.value)}>
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
              value={createForm.title}
              onChange={(e) => updateCreateForm('title', e.target.value)}
              placeholder="Focizas pentek este"
            />
          </label>

          <label>
            Leiras
            <textarea
              value={createForm.description}
              onChange={(e) => updateCreateForm('description', e.target.value)}
              placeholder="Negyen vagyunk, kell meg ketto."
            />
          </label>

          <div className="form-row">
            <label>
              Varos
              <input
                required
                value={createForm.city}
                onChange={(e) => updateCreateForm('city', e.target.value)}
                placeholder="Budapest"
              />
            </label>
            <label>
              Kezdes
              <input
                type="datetime-local"
                required
                value={createForm.startTime}
                onChange={(e) => updateCreateForm('startTime', e.target.value)}
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
                value={createForm.maxParticipants}
                onChange={(e) => updateCreateForm('maxParticipants', e.target.value)}
              />
            </label>
            <label>
              Min skill szint
              <select value={createForm.minSkillLevel} onChange={(e) => updateCreateForm('minSkillLevel', e.target.value)}>
                <option value="">(nincs megkotes)</option>
                <option value="BEGINNER">Kezdo</option>
                <option value="INTERMEDIATE">Halado</option>
                <option value="ADVANCED">Fejlett</option>
                <option value="PRO">Profi</option>
              </select>
            </label>
          </div>

          <div className="sheet-actions">
            <MotionButton type="button" className="btn-ghost" onClick={() => setCreateOpen(false)}>
              Megse
            </MotionButton>
            <MotionButton type="submit" className="btn-primary" disabled={creating}>
              {creating ? 'Letrehozas...' : 'Letrehozas'}
            </MotionButton>
          </div>
        </form>
      </BottomSheet>
    </div>
  );
}
