-- V15: tabela de auditoria de ações administrativas

CREATE TABLE admin_audit_log (
    id           BIGSERIAL    PRIMARY KEY,
    admin_id     BIGINT       NOT NULL,
    acao         VARCHAR(100) NOT NULL,
    recurso_tipo VARCHAR(100),
    recurso_id   BIGINT,
    dados_antes  TEXT,
    dados_depois TEXT,
    ip_address   VARCHAR(50),
    status       VARCHAR(20)  NOT NULL DEFAULT 'SUCESSO',
    criado_em    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_audit_admin_id   ON admin_audit_log(admin_id);
CREATE INDEX idx_audit_criado_em  ON admin_audit_log(criado_em DESC);
CREATE INDEX idx_audit_acao       ON admin_audit_log(acao);
