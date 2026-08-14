import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api/client.js';
import { RatingStarsDisplay } from '../components/RatingStars.jsx';
import ErrorBanner from '../components/ErrorBanner.jsx';

export default function UserProfilePage() {
  const { id } = useParams();
  const [profile, setProfile] = useState(null);
  const [ratings, setRatings] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    setError(null);
    Promise.all([api.users.getPublic(id), api.users.ratings(id)])
      .then(([p, r]) => {
        setProfile(p);
        setRatings(r);
      })
      .catch(setError)
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="page-loading">Betoltes...</div>;
  if (error) {
    return (
      <div className="page page-narrow">
        <ErrorBanner error={error} />
      </div>
    );
  }
  if (!profile) return null;

  return (
    <div className="page page-narrow">
      <div className="card profile-summary">
        <div>
          <h1>{profile.name}</h1>
        </div>
        <div className="profile-reliability">
          <RatingStarsDisplay value={profile.reliabilityScore} />
          <span>{Number(profile.reliabilityScore ?? 0).toFixed(2)} megbizhatosag</span>
        </div>
      </div>

      <div className="card">
        <h2>Ertekelesek ({ratings.length})</h2>
        {ratings.length === 0 ? (
          <p className="empty-state">Meg nincs ertekelese.</p>
        ) : (
          <ul className="rating-list">
            {ratings.map((r) => (
              <li key={r.id} className="rating-list-item">
                <div className="rating-list-header">
                  <RatingStarsDisplay value={r.stars} size="sm" />
                  <span className="muted">{r.rater?.name}</span>
                  {r.noShow && <span className="badge badge-noshow">Nem jelent meg</span>}
                </div>
                {r.tags?.length > 0 && (
                  <div className="rating-tags">
                    {r.tags.map((t) => (
                      <span key={t} className="tag">
                        {t}
                      </span>
                    ))}
                  </div>
                )}
                {r.comment && <p className="rating-comment">{r.comment}</p>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
