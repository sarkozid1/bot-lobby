CREATE TABLE lobbies (
    id                 UUID PRIMARY KEY,
    sport_id           UUID         NOT NULL REFERENCES sports(id) ON DELETE RESTRICT,
    creator_id         UUID         NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title              VARCHAR(255) NOT NULL,
    description        TEXT,
    city               VARCHAR(255) NOT NULL,
    start_time         TIMESTAMP    NOT NULL,
    max_participants   INTEGER      NOT NULL,
    min_skill_level    VARCHAR(20),
    status             VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    created_at         TIMESTAMP    NOT NULL,

    CONSTRAINT chk_lobbies_max_participants CHECK (max_participants > 0),
    CONSTRAINT chk_lobbies_min_skill_level CHECK (min_skill_level IS NULL OR min_skill_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'PRO')),
    CONSTRAINT chk_lobbies_status CHECK (status IN ('OPEN', 'FULL', 'CLOSED', 'CANCELLED'))
);

CREATE INDEX idx_lobbies_city ON lobbies (city);
CREATE INDEX idx_lobbies_sport_id ON lobbies (sport_id);
CREATE INDEX idx_lobbies_start_time ON lobbies (start_time);
CREATE INDEX idx_lobbies_creator_id ON lobbies (creator_id);
