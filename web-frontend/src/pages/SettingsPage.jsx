import { useState } from 'react';
import { getBaseUrl, setBaseUrl } from '../api/client.js';
import { useToast } from '../context/ToastContext.jsx';

export default function SettingsPage() {
  const [value, setValue] = useState(getBaseUrl());
  const toast = useToast();

  function handleSave(e) {
    e.preventDefault();
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
