// Egysegesen kinezo hibauzenet-doboz, amit szinte minden urlap/oldal hasznal - a
// api/client.js ApiError objektumat (vagy egyszeru string-et) fogadja, es azt jeleniti meg.
export default function ErrorBanner({ error }) {
  // Nincs hiba -> nincs mit renderelni. Ez teszi lehetove, hogy a hivo oldalak felteteles
  // logika nelkul, mindig kiirjak a <ErrorBanner error={error} />-t - ha error null, ez
  // egyszeruen nem jelenit meg semmit.
  if (!error) return null;
  // Az error lehet nyers String (ritkan) vagy egy ApiError peldany (a legtobbszor) -
  // mindket esetet kezeli.
  const message = typeof error === 'string' ? error : error.message;

  // A backend GlobalExceptionHandler.handleValidation altal kuldott mezonkenti hibak
  // (lasd ApiError.fieldErrors a backenden) - ha vannak, kulon, listazott formaban is kiirjuk oket.
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
