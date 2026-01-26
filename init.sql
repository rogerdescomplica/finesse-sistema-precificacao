-- Criar schema se não existir
CREATE SCHEMA IF NOT EXISTS public;

-- Configurar UTF-8
SET client_encoding = 'UTF8';

-- Configurar timezone
SET timezone = 'America/Sao_Paulo';

-- =============================================
-- 2. TABELAS
-- =============================================


-- -------------------------
-- Tabela: materiais
-- -------------------------
CREATE TABLE IF NOT EXISTS public.materiais (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto VARCHAR(200) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL,
    volume_embalagem NUMERIC(10,2) NOT NULL,
    preco_embalagem NUMERIC(10,2) NOT NULL,
    custo_unitario NUMERIC(10,6) GENERATED ALWAYS AS (preco_embalagem / volume_embalagem) STORED,
    observacoes VARCHAR(500),
    ativo BOOLEAN DEFAULT true NOT NULL,

    CONSTRAINT chk_materiais_volume CHECK (volume_embalagem > 0),
    CONSTRAINT chk_materiais_preco CHECK (preco_embalagem > 0)
);

-- -------------------------
-- Tabela: usuarios
-- -------------------------
CREATE TABLE IF NOT EXISTS public.usuarios (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT true NOT NULL
);

-- -------------------------
-- Tabela: usuario_perfis (N:N)
-- -------------------------
CREATE TABLE IF NOT EXISTS public.usuario_perfis (
    usuario_id BIGINT NOT NULL,
    perfil VARCHAR(50) NOT NULL,

    CONSTRAINT fk_usuario_perfis FOREIGN KEY (usuario_id)
        REFERENCES public.usuarios(id) ON DELETE CASCADE,

    CONSTRAINT pk_usuario_perfis PRIMARY KEY (usuario_id, perfil)
);

-- =============================================
-- 3. ÍNDICES
-- =============================================
CREATE INDEX IF NOT EXISTS idx_materiais_produto ON public.materiais(produto);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON public.usuarios(email);


-- =============================================
-- 5. TRIGGERS
-- =============================================

CREATE OR REPLACE FUNCTION public.trg_atualizar_custo_servico_material()
RETURNS TRIGGER AS $$
DECLARE
    v_custo NUMERIC;
BEGIN
    SELECT custo_unitario INTO v_custo
    FROM public.materiais
    WHERE id = NEW.material_id;

    NEW.custo_total := v_custo * NEW.quantidade_usada;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =============================================
-- FIM DO SCRIPT
-- =============================================

-- Inserir usuário administrador
INSERT INTO public.usuarios (nome, email, senha, ativo)
VALUES (
    'Administrador',
    'admin@sistema.com',
    '$2a$10$es.zMEVd7ASaVWWXiKaceOnX7hoEm3wHgIL9d5UjgcfPDpGxqSQtK',
    true
);

-- Atribuir perfil de administrador
INSERT INTO public.usuario_perfis (usuario_id, perfil)
VALUES (
    (SELECT id FROM public.usuarios WHERE email = 'admin@sistema.com'),
    'ADMIN'
);

-- =============================================
-- MATERIAIS PARA ESTÉTICA FACIAL E MASSAGEM
-- =============================================

-- Limpeza de Pele e Facial
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
VALUES
    ('Sabonete Facial Líquido', 'ml', 500.00, 85.00, 'Frasco de 500ml', true),
    ('Tônico Facial', 'ml', 500.00, 95.00, 'Frasco de 500ml', true),
    ('Esfoliante Facial', 'g', 500.00, 120.00, 'Pote de 500g', true),
    ('Máscara de Argila Verde', 'g', 500.00, 78.00, 'Pote de 500g', true),
    ('Máscara de Argila Branca', 'g', 500.00, 82.00, 'Pote de 500g', true);

-- Estética Facial
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
VALUES
    ('Sérum Vitamina C', 'ml', 30.00, 145.00, 'Frasco de 30ml', true),
    ('Sérum Ácido Hialurônico', 'ml', 30.00, 165.00, 'Frasco de 30ml', true),
    ('Creme Anti-idade', 'g', 50.00, 185.00, 'Pote de 50g', true),
    ('Creme Hidratante Facial', 'g', 200.00, 125.00, 'Pote de 200g', true),
    ('Gel Clareador', 'g', 30.00, 98.00, 'Bisnaga de 30g', true),
    ('Ampola Ácido Hialurônico', 'ml', 60.00, 220.00, 'Caixa com 12 ampolas de 5ml', true);

-- Massagem Corporal
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
VALUES
    ('Creme de Massagem Neutro', 'kg', 1.00, 68.00, 'Pote de 1kg', true),
    ('Gel Redutor', 'kg', 1.00, 145.00, 'Pote de 1kg', true),
    ('Gel Crioterápico', 'kg', 1.00, 125.00, 'Pote de 1kg', true),
    ('Loção Hidratante Corporal', 'l', 1.00, 65.00, 'Frasco de 1 litro', true);

-- Descartáveis e Higiene
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
VALUES
    ('Lençol Descartável', 'm', 50.00, 85.00, 'Rolo com 50 metros', true),
    ('Touca Descartável', 'un', 100.00, 28.00, 'Pacote com 100 unidades', true),
    ('Máscara Descartável', 'un', 50.00, 45.00, 'Caixa com 50 unidades', true),
    ('Luvas de Procedimento', 'un', 100.00, 42.00, 'Caixa com 100 unidades', true),
    ('Algodão em Disco', 'un', 100.00, 18.00, 'Pacote com 100 discos', true),
    ('Gaze Estéril', 'un', 100.00, 55.00, 'Caixa com 100 unidades', true),
    ('Espátula Descartável', 'un', 100.00, 35.00, 'Pacote com 100 unidades', true),
    ('Toalha de Rosto Branca', 'un', 12.00, 180.00, 'Pacote com 12 toalhas', true);

-- Equipamentos Consumíveis
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
VALUES
    ('Gel para Ultrassom', 'kg', 1.00, 68.00, 'Pote de 1kg', true),
    ('Gel Condutor Radiofrequência', 'kg', 1.00, 125.00, 'Pote de 1kg', true),
    ('Eletrodos Descartáveis', 'un', 50.00, 95.00, 'Pacote com 50 pares', true),
    ('Lâminas Dermaplaning', 'un', 10.00, 85.00, 'Caixa com 10 lâminas estéreis', true),
    ('Agulhas para Microagulhamento', 'un', 12.00, 180.00, 'Caixa com 12 cartuchos estéreis', true);

-- Hidratação e Finalização
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
VALUES
    ('Máscara Hidratante Facial', 'un', 10.00, 120.00, 'Caixa com 10 máscaras de tecido', true),
    ('Máscara de Colágeno', 'un', 10.00, 145.00, 'Caixa com 10 máscaras', true),
    ('Água Termal', 'ml', 150.00, 58.00, 'Spray de 150ml', true);


-- Tabela para a entidade Configuracoes

CREATE TABLE IF NOT EXISTS configuracoes (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,

    -- ====== Financeiro ======
    pretensao_salarial_mensal NUMERIC(14,2) NOT NULL,
    horas_semanais            NUMERIC(10,2) NOT NULL,
    semanas_media_mes         NUMERIC(10,4) NOT NULL DEFAULT 4.33,

    -- ====== Precificação ======
    custo_fixo_pct            NUMERIC(7,4)  NOT NULL,
    margem_lucro_padrao_pct   NUMERIC(7,4)  NOT NULL,

    atualizado_em             TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ativo                     BOOLEAN NOT NULL DEFAULT TRUE
);

-- (Opcional) Garantir que exista no máximo 1 registro ativo
-- Se você quiser permitir vários registros inativos e apenas 1 ativo:
CREATE UNIQUE INDEX IF NOT EXISTS uq_configuracoes_ativo_true
ON configuracoes ((ativo))
WHERE ativo IS TRUE;

INSERT INTO configuracoes (
    pretensao_salarial_mensal,
    horas_semanais,
    semanas_media_mes,
    custo_fixo_pct,
    margem_lucro_padrao_pct,
    atualizado_em,
    ativo
) VALUES (
    12000.00,        -- pretensão salarial mensal (12k)
    50.00,           -- horas semanais
    4.33,            -- semanas médias no mês
    12.0000,         -- custo fixo (%)
    20.0000,         -- margem de lucro padrão (%)
    CURRENT_TIMESTAMP,
    TRUE
);
