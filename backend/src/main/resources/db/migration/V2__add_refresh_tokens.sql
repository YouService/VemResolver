-- V2: tabela de refresh tokens para renovação automática de sessão

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES usuarios(id)
);
