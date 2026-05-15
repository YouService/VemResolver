-- V1: schema inicial do VemResolver (baseline — não executado em DBs existentes)

CREATE TABLE IF NOT EXISTS usuarios (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    login       VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    tipo        VARCHAR(50)  NOT NULL,
    telefone    VARCHAR(255),
    cpf         VARCHAR(255),
    rg          VARCHAR(255),
    orgao_expedidor     VARCHAR(255),
    data_expedicao_rg   VARCHAR(255),
    cep         VARCHAR(255),
    endereco    VARCHAR(255),
    cidade      VARCHAR(255),
    estado      VARCHAR(255),
    numero      VARCHAR(255),
    complemento VARCHAR(255),
    data_cadastro       TIMESTAMP(0),
    ativo               BOOLEAN  NOT NULL DEFAULT TRUE,
    avaliacao_media     FLOAT8   NOT NULL DEFAULT 0.0,
    total_avaliacoes    INTEGER  NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS categorias (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(255) NOT NULL UNIQUE,
    descricao TEXT
);

CREATE TABLE IF NOT EXISTS prestadores (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL UNIQUE,
    descricao_perfil TEXT,
    cidade          VARCHAR(255),
    estado          VARCHAR(255),
    avaliacao_media  FLOAT8  NOT NULL DEFAULT 0.0,
    total_avaliacoes INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_prestador_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS prestador_categorias (
    prestador_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    PRIMARY KEY (prestador_id, categoria_id),
    CONSTRAINT fk_pc_prestador FOREIGN KEY (prestador_id) REFERENCES prestadores(id),
    CONSTRAINT fk_pc_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS solicitacoes_servico (
    id           BIGSERIAL PRIMARY KEY,
    cliente_id   BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    descricao    TEXT   NOT NULL,
    cidade       VARCHAR(255) NOT NULL,
    estado       VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL DEFAULT 'ABERTA',
    data_criacao TIMESTAMP    NOT NULL,
    CONSTRAINT fk_ss_cliente   FOREIGN KEY (cliente_id)   REFERENCES usuarios(id),
    CONSTRAINT fk_ss_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE TABLE IF NOT EXISTS propostas (
    id             BIGSERIAL PRIMARY KEY,
    solicitacao_id BIGINT        NOT NULL,
    prestador_id   BIGINT        NOT NULL,
    valor          NUMERIC(10,2) NOT NULL,
    mensagem       TEXT,
    status         VARCHAR(50)   NOT NULL DEFAULT 'ENVIADA',
    data_envio     TIMESTAMP     NOT NULL,
    CONSTRAINT fk_prop_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes_servico(id),
    CONSTRAINT fk_prop_prestador   FOREIGN KEY (prestador_id)   REFERENCES prestadores(id)
);

CREATE TABLE IF NOT EXISTS avaliacoes (
    id             BIGSERIAL PRIMARY KEY,
    solicitacao_id BIGINT    NOT NULL,
    prestador_id   BIGINT    NOT NULL,
    cliente_id     BIGINT    NOT NULL,
    tipo           VARCHAR(50) NOT NULL DEFAULT 'CLIENTE_AVALIA_PRESTADOR',
    nota           INTEGER   NOT NULL,
    comentario     TEXT,
    data_criacao   TIMESTAMP NOT NULL,
    CONSTRAINT fk_aval_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes_servico(id),
    CONSTRAINT fk_aval_prestador   FOREIGN KEY (prestador_id)   REFERENCES prestadores(id),
    CONSTRAINT fk_aval_cliente     FOREIGN KEY (cliente_id)     REFERENCES usuarios(id)
);

CREATE TABLE IF NOT EXISTS verification_token (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(255),
    user_id     BIGINT NOT NULL,
    expiry_date TIMESTAMP,
    CONSTRAINT fk_vt_user FOREIGN KEY (user_id) REFERENCES usuarios(id)
);
