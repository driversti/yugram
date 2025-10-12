CREATE TABLE IF NOT EXISTS users
(
    id                BIGINT PRIMARY KEY,
    username          VARCHAR(255),
    first_name        VARCHAR(255),
    last_name         VARCHAR(255),
    phone_number      VARCHAR(20),
    is_contact        BOOLEAN     NOT NULL DEFAULT FALSE,
    is_mutual_contact BOOLEAN     NOT NULL DEFAULT FALSE,
    is_close_friend   BOOLEAN     NOT NULL DEFAULT FALSE,
    is_premium        BOOLEAN     NOT NULL DEFAULT FALSE,
    is_support        BOOLEAN     NOT NULL DEFAULT FALSE,
    type              VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
    language_code     VARCHAR(10),

    CONSTRAINT users_type_check CHECK (type IN ('BOT', 'REGULAR', 'DELETED', 'UNKNOWN'))
);