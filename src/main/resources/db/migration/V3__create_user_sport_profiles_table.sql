CREATE TABLE user_sport_profiles (
    id           UUID PRIMARY KEY,
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sport_id     UUID        NOT NULL REFERENCES sports(id) ON DELETE RESTRICT,
    skill_level  VARCHAR(20) NOT NULL,

    CONSTRAINT uq_user_sport_profiles_user_sport UNIQUE (user_id, sport_id),
    CONSTRAINT chk_user_sport_profiles_skill_level CHECK (skill_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'PRO'))
);

CREATE INDEX idx_user_sport_profiles_sport_id ON user_sport_profiles (sport_id);
