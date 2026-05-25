-- V22__add_fulltext_search.sql
-- Adiciona colunas tsvector para busca full-text em prestadores e solicitações

-- Adiciona coluna titulo em solicitacoes_servico se não existir
ALTER TABLE solicitacoes_servico
  ADD COLUMN IF NOT EXISTS titulo VARCHAR(255);

-- Adiciona colunas tsvector
ALTER TABLE prestadores
  ADD COLUMN IF NOT EXISTS ts_perfil TSVECTOR;

ALTER TABLE solicitacoes_servico
  ADD COLUMN IF NOT EXISTS ts_descricao TSVECTOR;

-- Popula os valores iniciais para prestadores
UPDATE prestadores
  SET ts_perfil = to_tsvector('portuguese',
    coalesce(nome_fantasia, '') || ' ' ||
    coalesce(descricao_perfil, '') || ' ' ||
    coalesce(cidade, '') || ' ' ||
    coalesce(estado, ''))
  WHERE ts_perfil IS NULL;

-- Popula os valores iniciais para solicitações
UPDATE solicitacoes_servico
  SET ts_descricao = to_tsvector('portuguese',
    coalesce(titulo, '') || ' ' ||
    coalesce(descricao, '') || ' ' ||
    coalesce(cidade, '') || ' ' ||
    coalesce(estado, ''))
  WHERE ts_descricao IS NULL;

-- Índices GIN para performance
CREATE INDEX IF NOT EXISTS idx_prestadores_ts_perfil ON prestadores USING GIN(ts_perfil);
CREATE INDEX IF NOT EXISTS idx_solicitacoes_ts_descricao ON solicitacoes_servico USING GIN(ts_descricao);

-- Trigger para manter ts_perfil atualizado automaticamente
CREATE OR REPLACE FUNCTION update_prestador_ts()
RETURNS TRIGGER AS $$
BEGIN
  NEW.ts_perfil := to_tsvector('portuguese',
    coalesce(NEW.nome_fantasia, '') || ' ' ||
    coalesce(NEW.descricao_perfil, '') || ' ' ||
    coalesce(NEW.cidade, '') || ' ' ||
    coalesce(NEW.estado, ''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_solicitacao_ts()
RETURNS TRIGGER AS $$
BEGIN
  NEW.ts_descricao := to_tsvector('portuguese',
    coalesce(NEW.titulo, '') || ' ' ||
    coalesce(NEW.descricao, '') || ' ' ||
    coalesce(NEW.cidade, '') || ' ' ||
    coalesce(NEW.estado, ''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_prestador_ts ON prestadores;
CREATE TRIGGER trg_prestador_ts
  BEFORE INSERT OR UPDATE ON prestadores
  FOR EACH ROW EXECUTE FUNCTION update_prestador_ts();

DROP TRIGGER IF EXISTS trg_solicitacao_ts ON solicitacoes_servico;
CREATE TRIGGER trg_solicitacao_ts
  BEFORE INSERT OR UPDATE ON solicitacoes_servico
  FOR EACH ROW EXECUTE FUNCTION update_solicitacao_ts();
