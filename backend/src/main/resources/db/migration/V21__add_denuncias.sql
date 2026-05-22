CREATE TABLE denuncias (
    id          BIGSERIAL PRIMARY KEY,
    denunciante_id BIGINT NOT NULL REFERENCES usuarios(id),
    denunciado_id  BIGINT NOT NULL REFERENCES usuarios(id),
    tipo        VARCHAR(30) NOT NULL CHECK (tipo IN ('PERFIL', 'AVALIACAO', 'COMPORTAMENTO')),
    descricao   TEXT NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA')),
    data_criacao TIMESTAMP(0) NOT NULL DEFAULT NOW(),
    data_revisao TIMESTAMP(0),
    observacao_admin TEXT,
    CONSTRAINT uq_denuncia_por_tipo UNIQUE (denunciante_id, denunciado_id, tipo)
);

CREATE INDEX idx_denuncia_denunciado ON denuncias(denunciado_id);
CREATE INDEX idx_denuncia_status      ON denuncias(status);
CREATE INDEX idx_denuncia_data        ON denuncias(data_criacao);
