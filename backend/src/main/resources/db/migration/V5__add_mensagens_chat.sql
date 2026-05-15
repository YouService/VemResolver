CREATE TABLE IF NOT EXISTS mensagens_chat (
    id              BIGSERIAL PRIMARY KEY,
    solicitacao_id  BIGINT      NOT NULL,
    remetente_id    BIGINT      NOT NULL,
    texto           TEXT        NOT NULL CHECK (char_length(texto) <= 2000),
    data_envio      TIMESTAMP   NOT NULL DEFAULT NOW(),
    lida            BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_chat_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes_servico(id),
    CONSTRAINT fk_chat_remetente   FOREIGN KEY (remetente_id)   REFERENCES usuarios(id)
);
CREATE INDEX IF NOT EXISTS idx_chat_solicitacao ON mensagens_chat(solicitacao_id, data_envio);
