ALTER TABLE prestadores ADD COLUMN IF NOT EXISTS foto_perfil_url TEXT;
ALTER TABLE prestadores ADD COLUMN IF NOT EXISTS percentual_completude INT NOT NULL DEFAULT 0;
