-- Adiciona ADMIN aos valores permitidos no campo tipo da tabela usuarios
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_tipo_check;
ALTER TABLE usuarios ADD CONSTRAINT usuarios_tipo_check
    CHECK (tipo IN ('CLIENTE', 'PRESTADOR', 'ADMIN'));
