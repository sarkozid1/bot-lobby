import { Link, useParams } from 'react-router-dom';
import LobbyDetailContent from '../components/LobbyDetailContent.jsx';
import { IconChevronLeft } from '../components/Icon.jsx';

// Teljes oldalas nezet - kozvetlen URL-megnyitashoz/frissiteshez.
// Amikor a feedbol kartyara kattintva erkezunk ide, ehelyett a LobbyDetailModal
// jelenik meg (lasd App.jsx backgroundLocation route-mintaja), ami a kartyabol indul.
export default function LobbyDetailPage() {
  const { id } = useParams();

  return (
    <div className="page page-narrow">
      <Link to="/lobbies" className="back-link">
        <IconChevronLeft size={14} /> Vissza a lobbykhoz
      </Link>
      <LobbyDetailContent id={id} />
    </div>
  );
}
