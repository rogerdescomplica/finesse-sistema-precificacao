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
-- Tabela: grupos
-- -------------------------
CREATE TABLE IF NOT EXISTS public.grupos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(500),
    ativo BOOLEAN DEFAULT true NOT NULL
);

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
-- Tabela: aliquotas_cnae
-- -------------------------
CREATE TABLE IF NOT EXISTS public.aliquotas_cnae (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    atividade VARCHAR(100) NOT NULL,
    cnae VARCHAR(20) NOT NULL UNIQUE,
    aliquota_total NUMERIC(5,2) NOT NULL,
    iss NUMERIC(5,2) NOT NULL,
    observacoes VARCHAR(200),
    ativo BOOLEAN DEFAULT true NOT NULL,

    CONSTRAINT chk_aliquota_total CHECK (aliquota_total >= 0),
    CONSTRAINT chk_iss CHECK (iss >= 0)
);

-- -------------------------
-- Tabela: servicos
-- -------------------------
CREATE TABLE IF NOT EXISTS public.servicos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,

    -- FKs devem ser BIGINT para manter compatibilidade
    grupo_id BIGINT,
    aliquota_cnae_id BIGINT,

    duracao INT NOT NULL,
    valor_hora NUMERIC(10,2) NOT NULL,
    custo_materiais NUMERIC(10,2) DEFAULT 0,
    custo_total NUMERIC(10,2),
    percentual_imposto NUMERIC(5,2),
    percentual_custo_fixo NUMERIC(5,2),
    percentual_margem_lucro NUMERIC(5,2),
    markup NUMERIC(10,4) DEFAULT 1 NOT NULL,
    valor_venda_sugerido NUMERIC(10,2),
    valor_venda_atual NUMERIC(10,2),
    lucro_liquido_rs NUMERIC(10,2),
    lucro_liquido_percentual NUMERIC(5,2),
    observacoes VARCHAR(500),
    ativo BOOLEAN DEFAULT true NOT NULL,

    CONSTRAINT fk_servicos_grupo FOREIGN KEY (grupo_id)
        REFERENCES public.grupos(id) ON DELETE SET NULL,

    CONSTRAINT fk_servicos_aliquota FOREIGN KEY (aliquota_cnae_id)
        REFERENCES public.aliquotas_cnae(id) ON DELETE SET NULL,

    CONSTRAINT chk_servicos_valor_hora CHECK (valor_hora > 0),
    CONSTRAINT chk_servicos_duracao CHECK (duracao > 0)
);

-- -------------------------
-- Tabela: servico_materiais (N:N)
-- -------------------------
CREATE TABLE IF NOT EXISTS public.servico_materiais (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- FKs devem acompanhar o tipo BIGINT dos IDs originais
    servico_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,

    quantidade_usada NUMERIC(10,4) NOT NULL,
    custo_total NUMERIC(10,6),

    CONSTRAINT fk_servico_materiais_servico FOREIGN KEY (servico_id)
        REFERENCES public.servicos(id) ON DELETE CASCADE,

    CONSTRAINT fk_servico_materiais_material FOREIGN KEY (material_id)
        REFERENCES public.materiais(id) ON DELETE CASCADE,

    CONSTRAINT chk_servico_materiais_qtde CHECK (quantidade_usada > 0),
    CONSTRAINT uk_servico_material UNIQUE (servico_id, material_id)
);

-- -------------------------
-- Tabela: despesas_fixas
-- -------------------------
CREATE TABLE IF NOT EXISTS public.despesas_fixas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN DEFAULT true NOT NULL,

    CONSTRAINT chk_despesas_valor CHECK (valor >= 0)
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
CREATE INDEX IF NOT EXISTS idx_grupos_nome ON public.grupos(nome);
CREATE INDEX IF NOT EXISTS idx_materiais_produto ON public.materiais(produto);
CREATE INDEX IF NOT EXISTS idx_aliquotas_cnae ON public.aliquotas_cnae(cnae);
CREATE INDEX IF NOT EXISTS idx_servicos_nome ON public.servicos(nome);
CREATE INDEX IF NOT EXISTS idx_servico_materiais_servico ON public.servico_materiais(servico_id);
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

CREATE TRIGGER trg_servico_materiais_custo
    BEFORE INSERT OR UPDATE OF quantidade_usada
    ON public.servico_materiais
    FOR EACH ROW
    EXECUTE FUNCTION public.trg_atualizar_custo_servico_material();

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

    -- =============================================
-- INSERÇÃO DE GRUPOS DE SERVIÇOS
-- =============================================

INSERT INTO public.grupos (nome, descricao, ativo)
VALUES
    ('Estética Facial', 'Serviços de estética facial e limpeza de pele', true),
    ('Estética Corporal', 'Massagens e Estética Corporal', true);

-- =============================================
-- INSERÇÃO DE ALÍQUOTAS CNAE
-- =============================================

INSERT INTO public.aliquotas_cnae (atividade, cnae, aliquota_total, iss, observacoes, ativo)
VALUES
    ('Estética e Beleza', '9602-5/02', 8.54, 2.00, 'Simples Nacional - Anexo III', true);

-- =============================================
-- INSERÇÃO DE SERVIÇOS DE ESTÉTICA
-- =============================================

-- Serviço 1: Limpeza de Pele Profunda
INSERT INTO public.servicos (
    nome, 
    grupo_id, 
    aliquota_cnae_id, 
    duracao, 
    valor_hora, 
    custo_materiais, 
    percentual_imposto, 
    percentual_custo_fixo, 
    percentual_margem_lucro,
    observacoes, 
    ativo
)
VALUES (
    'Limpeza de Pele Profunda',
    (SELECT id FROM public.grupos WHERE nome = 'Estética Facial'),
    (SELECT id FROM public.aliquotas_cnae WHERE cnae = '9602-5/02'),
    90, -- duração em minutos
    80.00, -- valor hora profissional
    25.00, -- custo estimado de materiais
    8.54, -- percentual de impostos
    15.00, -- percentual custo fixo
    30.00, -- percentual margem de lucro
    'Inclui extração de cravos, máscara e hidratação',
    true
);

-- Serviço 2: Peeling de Diamante
INSERT INTO public.servicos (
    nome, 
    grupo_id, 
    aliquota_cnae_id, 
    duracao, 
    valor_hora, 
    custo_materiais, 
    percentual_imposto, 
    percentual_custo_fixo, 
    percentual_margem_lucro,
    observacoes, 
    ativo
)
VALUES (
    'Peeling de Diamante',
    (SELECT id FROM public.grupos WHERE nome = 'Estética Facial'),
    (SELECT id FROM public.aliquotas_cnae WHERE cnae = '9602-5/02'),
    60,
    90.00,
    18.00,
    8.54,
    15.00,
    35.00,
    'Microdermoabrasão com ponteiras de diamante',
    true
);

-- Serviço 6: Massagem Relaxante Completa
INSERT INTO public.servicos (
    nome, 
    grupo_id, 
    aliquota_cnae_id, 
    duracao, 
    valor_hora, 
    custo_materiais, 
    percentual_imposto, 
    percentual_custo_fixo, 
    percentual_margem_lucro,
    observacoes, 
    ativo
)
VALUES (
    'Massagem Relaxante Completa',
    (SELECT id FROM public.grupos WHERE nome = 'Estética Corporal'),
    (SELECT id FROM public.aliquotas_cnae WHERE cnae = '9602-5/02'),
    60,
    85.00,
    12.00,
    8.54,
    15.00,
    32.00,
    'Massagem corporal completa com óleos essenciais',
    true
);

-- Serviço 7: Drenagem Linfática Corporal
INSERT INTO public.servicos (
    nome, 
    grupo_id, 
    aliquota_cnae_id, 
    duracao, 
    valor_hora, 
    custo_materiais, 
    percentual_imposto, 
    percentual_custo_fixo, 
    percentual_margem_lucro,
    observacoes, 
    ativo
)
VALUES (
    'Drenagem Linfática Corporal',
    (SELECT id FROM public.grupos WHERE nome = 'Estética Corporal'),
    (SELECT id FROM public.aliquotas_cnae WHERE cnae = '9602-5/02'),
    60,
    90.00,
    20.00,
    8.54,
    15.00,
    30.00,
    'Técnica manual para redução de retenção',
    true
);

-- Serviço 8: Massagem Modeladora
INSERT INTO public.servicos (
    nome, 
    grupo_id, 
    aliquota_cnae_id, 
    duracao, 
    valor_hora, 
    custo_materiais, 
    percentual_imposto, 
    percentual_custo_fixo, 
    percentual_margem_lucro,
    observacoes, 
    ativo
)
VALUES (
    'Massagem Modeladora',
    (SELECT id FROM public.grupos WHERE nome = 'Estética Corporal'),
    (SELECT id FROM public.aliquotas_cnae WHERE cnae = '9602-5/02'),
    60,
    95.00,
    28.00,
    8.54,
    15.00,
    33.00,
    'Reduz medidas e melhora contorno corporal',
    true
);

-- Serviço 10: Massagem Terapêutica
INSERT INTO public.servicos (
    nome, 
    grupo_id, 
    aliquota_cnae_id, 
    duracao, 
    valor_hora, 
    custo_materiais, 
    percentual_imposto, 
    percentual_custo_fixo, 
    percentual_margem_lucro,
    observacoes, 
    ativo
)
VALUES (
    'Massagem Terapêutica',
    (SELECT id FROM public.grupos WHERE nome = 'Estética Corporal'),
    (SELECT id FROM public.aliquotas_cnae WHERE cnae = '9602-5/02'),
    60,
    100.00,
    15.00,
    8.54,
    15.00,
    35.00,
    'Focada em tensões musculares e dores',
    true
);