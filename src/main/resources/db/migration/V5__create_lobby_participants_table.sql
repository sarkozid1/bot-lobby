CREATE TABLE lobby_participants (
    id         UUID        PRIMARY KEY,
    lobby_id   UUID        NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    joined_at  TIMESTAMP   NOT NULL,

    CONSTRAINT uq_lobby_participants_lobby_user UNIQUE (lobby_id, user_id),
    CONSTRAINT chk_lobby_participants_status CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'LEFT'))
);

CREATE INDEX idx_lobby_participants_user_id ON lobby_participants (user_id);
CREATE INDEX idx_lobby_participants_lobby_status ON lobby_participants (lobby_id, status);
