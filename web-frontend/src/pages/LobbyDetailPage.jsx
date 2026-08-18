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
      {/* Ugyanaz a tartalom-komponens, mint a modal valtozatban (LobbyDetailModal.jsx) -
          igy a lobby-reszletek logikaja EGY helyen van megirva, csak ket kulonbozo
          "kereteben" (teljes oldal vs. felugro modal) jelenik meg. */}
      <LobbyDetailContent id={id} />
    </div>
  );
}
