-- Adiciona coluna data_criacao à tabela avaliacoes (ausente por ter sido criada via Hibernate DDL antes do Flyway)

ALTER TABLE avaliacoes
    ADD COLUMN IF NOT EXISTS data_criacao TIMESTAMP;

UPDATE avaliacoes
    SET data_criacao = NOW()
    WHERE data_criacao IS NULL;

ALTER TABLE avaliacoes
    ALTER COLUMN data_criacao SET NOT NULL;
