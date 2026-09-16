INSERT INTO users (name, password_hash)
VALUES ('admin', '$2b$10$OU4yP40ioTqEd1N/4vv3le9o0ST1LA8pFvzNjoxvkqsRMMNkX98f6');

ALTER TABLE users ADD CONSTRAINT uq_users_name UNIQUE (name);