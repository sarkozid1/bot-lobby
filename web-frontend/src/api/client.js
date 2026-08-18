// Ez a fajl a teljes backend-kommunikacio EGYETLEN belepesi pontja - sehol mashol a
// frontendben nem hivodik kozvetlenul fetch(...) a backend fele (a WebSocket-chat kivetel,
// lasd api/ws.js). Igy egy helyen van a base URL kezelese, a JWT csatolasa es az
// egyseges hibakezeles.

const BASE_URL_KEY = 'botlobby.baseUrl';
const TOKEN_KEY = 'botlobby.token';
// Build-time env valtozo (production build-nel a deployolt backend URL-je) - a Settings
// oldalon barmikor felulirhato, ami localStorage-ban tarolodik es elsobbseget elvez.
const DEFAULT_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

// A backend URL-t localStorage-ban taroljuk (nem pl. React state-ben), hogy oldal-ujratoltes
// utan is megmaradjon, es hogy a nem-React fajlok (mint ez a client.js) is konnyen elerjek
// React kontextus nelkul.
export function getBaseUrl() {
  return localStorage.getItem(BASE_URL_KEY) || DEFAULT_BASE_URL;
}

export function setBaseUrl(url) {
  localStorage.setItem(BASE_URL_KEY, url);
}

// A JWT access token - szinten localStorage-ban, hogy bezart/ujranyitott bongeszo-fulnel is
// bejelentkezve maradjon a felhasznalo (nincs sajat "remember me" logika, ez mindig "emlekszik").
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    // Kijelentkezeskor null-lal hivjuk - ilyenkor toroljuk a kulcsot, nem ures stringet tarolunk.
    localStorage.removeItem(TOKEN_KEY);
  }
}

// Sajat hiba-osztaly: minden backend-hivasbol szarmazo hiba ezzel dobodik, igy a hivo
// (pl. egy oldal komponens) mindig biztosan tud .status-t es .body-t is olvasni rola,
// nem csak egy sima Error .message-et (lasd ErrorBanner.jsx, ami a .body.fieldErrors-t is kiirja).
export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

// Kozponti, minden hivas mogott futo segedfuggveny - ezt hasznalja az osszes `api.*` metodus
// lent. Nem exportalt (nincs `export` elotte), csak ezen a fajlon belul erheto el.
async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  // Ha van bejelentkezett felhasznalo (van tarolt token), minden kerelemhez automatikusan
  // hozzafuzzuk az Authorization fejlecet - a hivo helyeknek (pl. api.lobbies.get) sose kell
  // ezzel kulon torodniuk.
  if (token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(getBaseUrl() + path, {
      method,
      headers,
      // GET/DELETE kerelemnel altalaban nincs body - ilyenkor undefined marad, a fetch nem
      // kuld request body-t.
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  } catch (networkErr) {
    // A fetch() akkor dob kivetelt, ha a HALOZATI kapcsolat magat sem sikerult letrehozni
    // (pl. rossz base URL, nincs futo backend, CORS elutasitas meg a preflight elott) - ez
    // MEGKULONBOZTETENDO a "backend valaszolt, de hibaval" esettol (lasd lent, res.ok).
    throw new ApiError(
      `Nem sikerult elerni a szervert (${getBaseUrl()}). Fut a backend, es engedi a CORS-t?`,
      0,
      null
    );
  }

  // A valasz testet elobb NYERS szovegkent olvassuk ki (nem res.json()-nal), mert nem minden
  // valasznak van teste (pl. 204 No Content), es JSON.parse(''), az hibat dobna.
  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      // Nem JSON valasz (pl. egy nyers szoveges hibauzenet) - akkor is hasznaljuk fel szovegkent.
      data = text;
    }
  }

  // res.ok: false minden 4xx/5xx HTTP statusznal - a backend GlobalExceptionHandler-je altal
  // kuldott ApiError JSON-bol (lasd ott) itt olvassuk ki a felhasznalobarat uzenetet.
  if (!res.ok) {
    const message = (data && data.message) || `Hiba tortent (${res.status}).`;
    throw new ApiError(message, res.status, data);
  }

  return data;
}

// A teljes API-felulet EGY, egymasba agyazott objektumkent - a hivo oldalon `api.lobbies.join(id)`
// alakban olvashato, ami kozvetlenul tukrozi a backend URL-strukturajat
// (POST /api/lobbies/{id}/join). Minden bejegyzes csak egy vekony wrapper a request(...) korul.
export const api = {
  auth: {
    register: (payload) => request('/api/auth/register', { method: 'POST', body: payload }),
    login: (payload) => request('/api/auth/login', { method: 'POST', body: payload })
  },
  sports: {
    list: () => request('/api/sports')
  },
  users: {
    me: () => request('/api/users/me'),
    updateMe: (payload) => request('/api/users/me', { method: 'PUT', body: payload }),
    upsertSportProfile: (payload) =>
      request('/api/users/me/sport-profiles', { method: 'POST', body: payload }),
    getPublic: (userId) => request(`/api/users/${userId}`),
    ratings: (userId) => request(`/api/users/${userId}/ratings`)
  },
  lobbies: {
    create: (payload) => request('/api/lobbies', { method: 'POST', body: payload }),
    // A szuresi parameterek (sport/city/date/skillLevel) objektumkent erkeznek - itt alakulnak
    // URL query stringge. Az Object.entries(...).filter(...) kiszuri az ures ('') vagy
    // hianyzo (null/undefined) ertekeket, hogy ne kuldjunk felesleges "?sport=&city=" parametereket.
    search: (params) => {
      const query = new URLSearchParams(
        Object.fromEntries(Object.entries(params || {}).filter(([, v]) => v !== '' && v != null))
      );
      const qs = query.toString();
      return request(`/api/lobbies${qs ? `?${qs}` : ''}`);
    },
    get: (id) => request(`/api/lobbies/${id}`),
    update: (id, payload) => request(`/api/lobbies/${id}`, { method: 'PUT', body: payload }),
    cancel: (id) => request(`/api/lobbies/${id}`, { method: 'DELETE' }),
    join: (id) => request(`/api/lobbies/${id}/join`, { method: 'POST' }),
    leave: (id) => request(`/api/lobbies/${id}/leave`, { method: 'POST' }),
    participants: (id) => request(`/api/lobbies/${id}/participants`),
    accept: (id, userId) => request(`/api/lobbies/${id}/participants/${userId}/accept`, { method: 'POST' }),
    decline: (id, userId) => request(`/api/lobbies/${id}/participants/${userId}/decline`, { method: 'POST' }),
    // Az uzenet-ELOZMENY meg ezen a REST vegponton at jon - az ELO uzenetek mar a WebSocket-en
    // (lasd api/ws.js), nem itt.
    messages: (id) => request(`/api/lobbies/${id}/messages`),
    rate: (id, payload) => request(`/api/lobbies/${id}/ratings`, { method: 'POST', body: payload })
  }
};
