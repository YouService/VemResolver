ALTER TABLE prestadores
    ADD COLUMN IF NOT EXISTS latitude  DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

CREATE TABLE IF NOT EXISTS notificacoes (
    id           BIGSERIAL PRIMARY KEY,
    usuario_id   BIGINT       NOT NULL,
    mensagem     TEXT         NOT NULL,
    tipo         VARCHAR(50)  NOT NULL,
    lida         BOOLEAN      NOT NULL DEFAULT FALSE,
    link_destino VARCHAR(255),
    data_criacao TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notif_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
CREATE INDEX IF NOT EXISTS idx_notif_usuario_lida ON notificacoes(usuario_id, lida);
