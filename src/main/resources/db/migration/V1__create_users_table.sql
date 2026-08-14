CREATE TABLE users (
    id                   UUID PRIMARY KEY,
    name                 VARCHAR(255)     NOT NULL,
    email                VARCHAR(255)     NOT NULL,
    password_hash        VARCHAR(255)     NOT NULL,
    profile_picture_url  VARCHAR(1024),
    city                 VARCHAR(255),
    reliability_score    DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    created_at           TIMESTAMP        NOT NULL,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_reliability_score CHECK (reliability_score >= 0.0 AND reliability_score <= 5.0)
);

CREATE INDEX idx_users_city ON users (city);
