-- Adiciona badge de identidade verificada (admin manual) separado do badge automático
ALTER TABLE prestadores ADD COLUMN id_verificado BOOLEAN NOT NULL DEFAULT FALSE;

-- Tabela de verificações de documento enviadas pelos prestadores
CREATE TABLE verificacoes_documento (
    id            BIGSERIAL PRIMARY KEY,
    prestador_id  BIGINT    NOT NULL REFERENCES prestadores(id),
    caminho_doc   TEXT      NOT NULL,
    caminho_selfie TEXT     NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                  CHECK (status IN ('PENDENTE', 'APROVADO', 'RECUSADO')),
    data_envio    TIMESTAMP NOT NULL DEFAULT NOW(),
    data_revisao  TIMESTAMP,
    observacao_admin TEXT
);

CREATE INDEX idx_verif_status      ON verificacoes_documento(status);
CREATE INDEX idx_verif_prestador   ON verificacoes_documento(prestador_id);
CREATE INDEX idx_verif_data_envio  ON verificacoes_documento(data_envio DESC);
