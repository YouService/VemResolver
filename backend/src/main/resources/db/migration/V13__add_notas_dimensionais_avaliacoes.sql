ALTER TABLE avaliacoes ADD COLUMN IF NOT EXISTS nota_pontualidade INTEGER;
ALTER TABLE avaliacoes ADD COLUMN IF NOT EXISTS nota_qualidade     INTEGER;
ALTER TABLE avaliacoes ADD COLUMN IF NOT EXISTS nota_comunicacao   INTEGER;
