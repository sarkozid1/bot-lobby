import { useState } from 'react';
import { getBaseUrl, setBaseUrl } from '../api/client.js';
import { useToast } from '../context/ToastContext.jsx';

// Fejlesztoi/tesztelesi segedoldal: itt allithato at futasidoben, MELYIK backend-peldanyt
// hasznalja a frontend (lasd api/client.js getBaseUrl/setBaseUrl - localStorage-ban tarolt
// felulirasa a build-time VITE_API_BASE_URL-nek). Nincs vedve ProtectedRoute-tal - meg
// bejelentkezes elott is elerheto kell legyen, pl. ha helyi backendre kell atallitani.
export default function SettingsPage() {
  const [value, setValue] = useState(getBaseUrl());
  const toast = useToast();

  function handleSave(e) {
    e.preventDefault();
    // A vegen levo "/" karaktert levagja (ha van), hogy a kesobbi URL-osszefuzesnel
    // (getBaseUrl() + path, lasd api/client.js) ne keletkezzen dupla "//".
    setBaseUrl(value.trim().replace(/\/$/, ''));
    toast.success('Backend URL elmentve. Frissitsd az oldalt, ha mar be voltal jelentkezve.');
  }

  return (
    <div className="auth-page">
      <form className="card auth-card" onSubmit={handleSave}>
        <h1>Beallitasok</h1>
        <label>
          Backend base URL
          <input value={value} onChange={(e) => setValue(e.target.value)} placeholder="http://localhost:8080" />
        </label>
        <button className="btn btn-primary btn-block" type="submit">
          Mentes
        </button>
      </form>
    </div>
  );
}
