CREATE TABLE IF NOT EXISTS messages
(
    id         BIGINT PRIMARY KEY,
    sender_id  BIGINT  NOT NULL,
    chat_id    BIGINT  NOT NULL,
    date       INTEGER NOT NULL,
    content    TEXT
);