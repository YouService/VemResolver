-- Adapta o chat para ser por proposta (não mais por solicitação)
-- Permite comunicação antes do aceite da proposta

ALTER TABLE mensagens_chat
    ADD COLUMN IF NOT EXISTS proposta_id BIGINT,
    ALTER COLUMN solicitacao_id DROP NOT NULL;

ALTER TABLE mensagens_chat
    ADD CONSTRAINT fk_chat_proposta
    FOREIGN KEY (proposta_id) REFERENCES propostas(id);

DROP INDEX IF EXISTS idx_chat_solicitacao;
CREATE INDEX IF NOT EXISTS idx_chat_proposta ON mensagens_chat(proposta_id, data_envio);
