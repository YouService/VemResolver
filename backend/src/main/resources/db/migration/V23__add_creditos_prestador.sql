-- V23: sistema de créditos pré-pagos do prestador (P6.1)

CREATE TABLE pacotes_credito (
    id                 BIGSERIAL    PRIMARY KEY,
    nome               VARCHAR(100) NOT NULL,
    descricao          TEXT,
    quantidade_creditos INTEGER     NOT NULL CHECK (quantidade_creditos > 0),
    preco_centavos     BIGINT       NOT NULL CHECK (preco_centavos >= 0),
    ativo              BOOLEAN      NOT NULL DEFAULT TRUE,
    data_criacao       TIMESTAMP(0) NOT NULL DEFAULT NOW()
);

CREATE TABLE contas_credito (
    id                   BIGSERIAL    PRIMARY KEY,
    prestador_id         BIGINT       NOT NULL UNIQUE REFERENCES prestadores(id),
    saldo                INTEGER      NOT NULL DEFAULT 0 CHECK (saldo >= 0),
    gratuitos_usados     INTEGER      NOT NULL DEFAULT 0,
    total_gasto_centavos BIGINT       NOT NULL DEFAULT 0,
    data_atualizacao     TIMESTAMP(0) NOT NULL DEFAULT NOW()
);

CREATE TABLE transacoes_credito (
    id                 BIGSERIAL    PRIMARY KEY,
    conta_id           BIGINT       NOT NULL REFERENCES contas_credito(id),
    tipo               VARCHAR(20)  NOT NULL CHECK (tipo IN ('COMPRA', 'CONSUMO', 'CONSUMO_GRATUITO')),
    status             VARCHAR(20)  NOT NULL CHECK (status IN ('PENDENTE', 'APROVADA', 'RECUSADA')),
    quantidade         INTEGER      NOT NULL,
    valor_centavos     BIGINT,
    saldo_apos         INTEGER,
    descricao          VARCHAR(255),
    pacote_id          BIGINT       REFERENCES pacotes_credito(id),
    proposta_id        BIGINT       REFERENCES propostas(id),
    gateway            VARCHAR(30),
    referencia_externa VARCHAR(100) UNIQUE,
    data_criacao       TIMESTAMP(0) NOT NULL DEFAULT NOW(),
    data_confirmacao   TIMESTAMP(0)
);

CREATE INDEX idx_transacao_credito_conta ON transacoes_credito(conta_id, data_criacao DESC);
