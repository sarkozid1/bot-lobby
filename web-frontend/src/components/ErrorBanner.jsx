export default function ErrorBanner({ error }) {
  if (!error) return null;
  const message = typeof error === 'string' ? error : error.message;

  const fieldErrors = error?.body?.fieldErrors;

  return (
    <div className="error-banner">
      <strong>Hiba:</strong> {message}
      {fieldErrors && (
        <ul>
          {Object.entries(fieldErrors).map(([field, msg]) => (
            <li key={field}>
              {field}: {msg}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
