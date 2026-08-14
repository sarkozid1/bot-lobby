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

export function SkillBadge({ level }) {
  if (!level) return null;
  return <span className={`badge badge-skill-${level.toLowerCase()}`}>{SKILL_LABEL[level] || level}</span>;
}
