CREATE TABLE IF NOT EXISTS accounts (
    login      VARCHAR(128) PRIMARY KEY,
    name       VARCHAR(256) NOT NULL,
    birthdate  DATE         NOT NULL,
    balance    BIGINT       NOT NULL
);
