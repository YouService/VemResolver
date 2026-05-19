CREATE TABLE IF NOT EXISTS configuracoes_email (
    id           BIGSERIAL PRIMARY KEY,
    brevo_api_key      TEXT,
    email_remetente    VARCHAR(255),
    nome_remetente     VARCHAR(100),
    ativo              BOOLEAN      NOT NULL DEFAULT FALSE,
    atualizado_em      TIMESTAMP
);

INSERT INTO configuracoes_email (brevo_api_key, email_remetente, nome_remetente, ativo)
VALUES (NULL, NULL, 'VemResolver', FALSE);
