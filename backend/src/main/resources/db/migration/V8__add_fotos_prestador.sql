CREATE TABLE IF NOT EXISTS fotos_prestador (
    id           BIGSERIAL    PRIMARY KEY,
    prestador_id BIGINT       NOT NULL,
    url          VARCHAR(500) NOT NULL,
    data_upload  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_foto_prestador FOREIGN KEY (prestador_id) REFERENCES prestadores(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_fotos_prestador ON fotos_prestador(prestador_id, data_upload);
