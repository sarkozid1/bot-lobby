CREATE TABLE ratings (
    id             UUID      PRIMARY KEY,
    lobby_id       UUID      NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE,
    rater_id       UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rated_user_id  UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stars          INTEGER   NOT NULL,
    comment        TEXT,
    no_show        BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP NOT NULL,

    CONSTRAINT chk_ratings_stars CHECK (stars BETWEEN 1 AND 5),
    CONSTRAINT chk_ratings_rater_not_rated_user CHECK (rater_id <> rated_user_id),
    CONSTRAINT uq_ratings_lobby_rater_rated_user UNIQUE (lobby_id, rater_id, rated_user_id)
);

CREATE TABLE rating_tags (
    rating_id  UUID        NOT NULL REFERENCES ratings(id) ON DELETE CASCADE,
    tag        VARCHAR(50) NOT NULL
);

CREATE INDEX idx_ratings_rated_user_id ON ratings (rated_user_id);
CREATE INDEX idx_ratings_lobby_id ON ratings (lobby_id);
CREATE INDEX idx_rating_tags_rating_id ON rating_tags (rating_id);
