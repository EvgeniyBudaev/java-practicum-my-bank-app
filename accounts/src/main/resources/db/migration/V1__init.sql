CREATE TABLE IF NOT EXISTS accounts (
    login      varchar(128) PRIMARY KEY,
    name       varchar(256) NOT NULL,
    birthdate  date         NOT NULL,
    balance    bigint       NOT NULL
);

INSERT INTO accounts (login, name, birthdate, balance)
VALUES
  ('petrov',  'Петров Петя',  DATE '1990-01-01',  10000),
  ('olegov', 'Олегов Олег', DATE '1985-05-10', 5000),
  ('ivanov',  'Иванов Иван',  DATE '1992-09-20',  2000)
ON CONFLICT (login) DO NOTHING;
