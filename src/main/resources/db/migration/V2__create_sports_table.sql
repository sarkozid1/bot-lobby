CREATE TABLE sports (
    id        UUID PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    icon_url  VARCHAR(1024),

    CONSTRAINT uq_sports_name UNIQUE (name)
);
