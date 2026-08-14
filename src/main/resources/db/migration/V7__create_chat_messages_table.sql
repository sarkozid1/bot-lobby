CREATE TABLE chat_messages (
    id         UUID      PRIMARY KEY,
    lobby_id   UUID      NOT NULL REFERENCES lobbies(id) ON DELETE CASCADE,
    sender_id  UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content    TEXT      NOT NULL,
    sent_at    TIMESTAMP NOT NULL
);

CREATE INDEX idx_chat_messages_lobby_sent_at ON chat_messages (lobby_id, sent_at);
