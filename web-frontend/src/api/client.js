const BASE_URL_KEY = 'botlobby.baseUrl';
const TOKEN_KEY = 'botlobby.token';
const DEFAULT_BASE_URL = 'http://localhost:8080';

export function getBaseUrl() {
  return localStorage.getItem(BASE_URL_KEY) || DEFAULT_BASE_URL;
}

export function setBaseUrl(url) {
  localStorage.setItem(BASE_URL_KEY, url);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(getBaseUrl() + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined
    });
  } catch (networkErr) {
    throw new ApiError(
      `Nem sikerult elerni a szervert (${getBaseUrl()}). Fut a backend, es engedi a CORS-t?`,
      0,
      null
    );
  }

  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!res.ok) {
    const message = (data && data.message) || `Hiba tortent (${res.status}).`;
    throw new ApiError(message, res.status, data);
  }

  return data;
}

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
    messages: (id) => request(`/api/lobbies/${id}/messages`),
    rate: (id, payload) => request(`/api/lobbies/${id}/ratings`, { method: 'POST', body: payload })
  }
};
