CREATE TABLE configuracoes_push (
    id BIGSERIAL PRIMARY KEY,
    vapid_public_key TEXT,
    vapid_private_key TEXT,
    atualizado_em TIMESTAMP(0)
);
INSERT INTO configuracoes_push DEFAULT VALUES;

CREATE TABLE push_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    endpoint TEXT NOT NULL,
    p256dh TEXT NOT NULL,
    auth TEXT NOT NULL,
    criado_em TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(usuario_id, endpoint)
);
CREATE INDEX idx_push_sub_usuario ON push_subscriptions(usuario_id);
