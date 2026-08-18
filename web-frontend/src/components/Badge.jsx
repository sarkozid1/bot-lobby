// Kis, ujrafelhasznalhato "cimke" komponensek a backend enum-ertekeinek (LobbyStatus,
// ParticipantStatus, SkillLevel) magyar, olvashato es szinezett megjelenitesehez - a nyers
// "OPEN"/"PENDING"/"BEGINNER" angol enum-nevek sose kerulnek kozvetlenul a felhasznalo ele.

const LOBBY_STATUS_LABEL = {
  OPEN: 'Nyitott',
  FULL: 'Megtelt',
  CLOSED: 'Lezart',
  CANCELLED: 'Lemondva'
};

const PARTICIPANT_STATUS_LABEL = {
  PENDING: 'Fuggoben',
  ACCEPTED: 'Elfogadva',
  DECLINED: 'Elutasitva',
  LEFT: 'Kilepett'
};

const SKILL_LABEL = {
  BEGINNER: 'Kezdo',
  INTERMEDIATE: 'Halado',
  ADVANCED: 'Fejlett',
  PRO: 'Profi'
};

// A CSS osztaly (pl. "badge-lobby-open") a nyers, KISBETUSITETT enum ertekbol epul fel -
// a szinezest a styles/index.css vegzi ez alapjan (lasd .badge-lobby-* szabalyokat).
// A `LOBBY_STATUS_LABEL[status] || status` biztonsagi halo: ha egy ismeretlen/uj enum
// ertek erkezne (amit meg nem vettunk fel a forditasi terkepbe), legalabb a nyers erteket
// mutatja, nem tunik el csendben semmi.
export function LobbyStatusBadge({ status }) {
  return <span className={`badge badge-lobby-${status?.toLowerCase()}`}>{LOBBY_STATUS_LABEL[status] || status}</span>;
}

export function ParticipantStatusBadge({ status }) {
  return (
    <span className={`badge badge-participant-${status?.toLowerCase()}`}>
      {PARTICIPANT_STATUS_LABEL[status] || status}
    </span>
  );
}

// Ez opcionalis mezobol jon (Lobby.minSkillLevel lehet null - "nincs megkotes") - ha nincs
// ertek, a komponens egyszeruen null-t ad vissza, azaz semmit nem renderel.
export function SkillBadge({ level }) {
  if (!level) return null;
  return <span className={`badge badge-skill-${level.toLowerCase()}`}>{SKILL_LABEL[level] || level}</span>;
}
