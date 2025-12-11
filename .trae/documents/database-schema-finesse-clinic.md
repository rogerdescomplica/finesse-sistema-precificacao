# Finesse Centro Integrado - Esquema de Banco de Dados

## 1. Visão Geral do Schema

O banco de dados PostgreSQL do Finesse Centro Integrado foi projetado para suportar operações de clínica com ênfase em controle financeiro, gestão de serviços, materiais e cálculos de precificação com base em alíquotas fiscais por CNAE.

## 2. Tabelas de Autenticação e Autorização

### 2.1 Tabela clinicas
```sql
CREATE TABLE clinicas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telefone VARCHAR(20),
    endereco TEXT,
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(8),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_clinicas_cnpj ON clinicas(cnpj);
CREATE INDEX idx_clinicas_email ON clinicas(email);
```

### 2.2 Tabela usuarios
```sql
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    papel VARCHAR(50) NOT NULL CHECK (papel IN ('PROPRIETARIO', 'ADMINISTRADOR', 'FUNCIONARIO')),
    ativo BOOLEAN DEFAULT true,
    email_verificado BOOLEAN DEFAULT false,
    ultimo_login TIMESTAMP WITH TIME ZONE,
    tentativas_login_falhado INTEGER DEFAULT 0,
    bloqueado_ate TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_usuarios_clinica_id ON usuarios(clinica_id);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_papel ON usuarios(papel);
```

### 2.3 Tabela sessoes_usuario
```sql
CREATE TABLE sessoes_usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_acesso VARCHAR(500) NOT NULL,
    token_refresh VARCHAR(500) NOT NULL,
    endereco_ip INET,
    user_agent TEXT,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    refresh_expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado BOOLEAN DEFAULT false,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_sessoes_usuario_usuario_id ON sessoes_usuario(usuario_id);
CREATE INDEX idx_sessoes_usuario_token_acesso ON sessoes_usuario(token_acesso);
CREATE INDEX idx_sessoes_usuario_token_refresh ON sessoes_usuario(token_refresh);
```

## 3. Tabelas de Configuração Fiscal

### 3.1 Tabela cnae
```sql
CREATE TABLE cnae (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    iss_rate DECIMAL(5,2) DEFAULT 0 CHECK (iss_rate >= 0 AND iss_rate <= 100),
    icms_rate DECIMAL(5,2) DEFAULT 0 CHECK (icms_rate >= 0 AND icms_rate <= 100),
    pis_rate DECIMAL(5,2) DEFAULT 0 CHECK (pis_rate >= 0 AND pis_rate <= 100),
    cofins_rate DECIMAL(5,2) DEFAULT 0 CHECK (cofins_rate >= 0 AND cofins_rate <= 100),
    irpj_rate DECIMAL(5,2) DEFAULT 0 CHECK (irpj_rate >= 0 AND irpj_rate <= 100),
    csll_rate DECIMAL(5,2) DEFAULT 0 CHECK (csll_rate >= 0 AND csll_rate <= 100),
    simples_nacional_rate DECIMAL(5,2) DEFAULT 0,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Inserir CNAE padrão para clínicas de estética
INSERT INTO cnae (code, description, iss_rate, pis_rate, cofins_rate, irpj_rate, csll_rate) VALUES
('9609-2/01', 'Estética e atividades de complementação estética', 5.0, 0.65, 3.0, 1.5, 1.0),
('9609-2/02', 'Atividades de estética médica', 2.0, 0.65, 3.0, 1.5, 1.0),
('86.90-6/01', 'Serviços médicos não especificados', 0.0, 0.65, 3.0, 1.5, 1.0),
('9602-3/01', 'Cabeleireiros, manicure e pedicure', 5.0, 0.65, 3.0, 1.5, 1.0),
('9602-3/02', 'Atividades de estética e outros serviços de cuidados com a beleza', 5.0, 0.65, 3.0, 1.5, 1.0);
```

### 3.2 Tabela configuracao_cnae_clinica
```sql
CREATE TABLE configuracao_cnae_clinica (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    cnae_id UUID NOT NULL REFERENCES cnae(id) ON DELETE CASCADE,
    aliquota_iss_personalizada DECIMAL(5,2),
    aliquota_icms_personalizada DECIMAL(5,2),
    aliquota_pis_personalizada DECIMAL(5,2),
    aliquota_cofins_personalizada DECIMAL(5,2),
    aliquota_irpj_personalizada DECIMAL(5,2),
    aliquota_csll_personalizada DECIMAL(5,2),
    data_vigencia DATE NOT NULL,
    ativo BOOLEAN DEFAULT true,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(clinica_id, cnae_id, data_vigencia)
);

CREATE INDEX idx_configuracao_cnae_clinica_clinica_id ON configuracao_cnae_clinica(clinica_id);
CREATE INDEX idx_configuracao_cnae_clinica_cnae_id ON configuracao_cnae_clinica(cnae_id);
```

## 4. Tabelas de Serviços e Precificação

### 4.1 Tabela servicos
```sql
CREATE TABLE servicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    cnae_id UUID REFERENCES cnae(id),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    duracao INTEGER NOT NULL CHECK (duracao > 0), -- em minutos
    categoria VARCHAR(100) NOT NULL,
    preco_base DECIMAL(10,2),
    preco_sugerido DECIMAL(10,2),
    preco_minimo DECIMAL(10,2),
    ativo BOOLEAN DEFAULT true,
    criado_por UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_servicos_clinica_id ON servicos(clinica_id);
CREATE INDEX idx_servicos_categoria ON servicos(categoria);
CREATE INDEX idx_servicos_ativo ON servicos(ativo);
CREATE INDEX idx_servicos_nome ON servicos(nome);
```

### 4.2 Tabela servico_materiais
```sql
CREATE TABLE servico_materiais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    servico_id UUID NOT NULL REFERENCES servicos(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES materiais(id) ON DELETE CASCADE,
    quantidade DECIMAL(10,3) NOT NULL CHECK (quantidade > 0),
    unidade VARCHAR(50) NOT NULL,
    percentual_perda DECIMAL(5,2) DEFAULT 0 CHECK (percentual_perda >= 0 AND percentual_perda <= 100),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(servico_id, material_id)
);

CREATE INDEX idx_servico_materiais_servico_id ON servico_materiais(servico_id);
CREATE INDEX idx_servico_materiais_material_id ON servico_materiais(material_id);
```

### 4.3 Tabela historico_precos
```sql
CREATE TABLE historico_precos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    servico_id UUID NOT NULL REFERENCES servicos(id) ON DELETE CASCADE,
    custo_direto DECIMAL(10,2) NOT NULL,
    custo_indireto DECIMAL(10,2) NOT NULL,
    custo_total DECIMAL(10,2) NOT NULL,
    percentual_markup DECIMAL(5,2) NOT NULL,
    valor_markup DECIMAL(10,2) NOT NULL,
    valor_impostos DECIMAL(10,2) NOT NULL,
    preco_final DECIMAL(10,2) NOT NULL,
    margem_lucro DECIMAL(5,2) NOT NULL,
    aliquotas_cnae JSONB, -- Armazena as alíquotas aplicadas
    calculado_por UUID REFERENCES usuarios(id),
    data_calculo TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_historico_precos_servico_id ON historico_precos(servico_id);
CREATE INDEX idx_historico_precos_data_calculo ON historico_precos(data_calculo);
```

## 5. Tabelas de Materiais e Estoque

### 5.1 Tabela fornecedores
```sql
CREATE TABLE fornecedores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    cpf VARCHAR(11),
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco TEXT,
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(8),
    prazo_pagamento INTEGER DEFAULT 0, -- dias para pagamento
    prazo_entrega INTEGER DEFAULT 0, -- dias para entrega
    ativo BOOLEAN DEFAULT true,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT check_documento CHECK (
        (cnpj IS NOT NULL AND cpf IS NULL) OR 
        (cpf IS NOT NULL AND cnpj IS NULL)
    )
);

CREATE INDEX idx_fornecedores_clinica_id ON fornecedores(clinica_id);
CREATE INDEX idx_fornecedores_nome ON fornecedores(nome);
```

### 5.2 Tabela materiais
```sql
CREATE TABLE materiais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    fornecedor_id UUID REFERENCES fornecedores(id) ON DELETE SET NULL,
    sku VARCHAR(100) UNIQUE,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    unidade VARCHAR(50) NOT NULL,
    custo_unitario DECIMAL(10,2) NOT NULL CHECK (custo_unitario >= 0),
    estoque_atual INTEGER NOT NULL DEFAULT 0 CHECK (estoque_atual >= 0),
    estoque_minimo INTEGER NOT NULL DEFAULT 0 CHECK (estoque_minimo >= 0),
    estoque_maximo INTEGER DEFAULT 1000,
    ponto_reposicao INTEGER DEFAULT 0,
    localizacao VARCHAR(100), -- localização no estoque
    categoria VARCHAR(100),
    codigo_barras VARCHAR(100),
    data_validade DATE,
    numero_lote VARCHAR(100),
    ativo BOOLEAN DEFAULT true,
    criado_por UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_materiais_clinica_id ON materiais(clinica_id);
CREATE INDEX idx_materiais_fornecedor_id ON materiais(fornecedor_id);
CREATE INDEX idx_materiais_sku ON materiais(sku);
CREATE INDEX idx_materiais_nome ON materiais(nome);
CREATE INDEX idx_materiais_categoria ON materiais(categoria);
CREATE INDEX idx_materiais_ativo ON materiais(ativo);
```

### 5.3 Tabela movimentacoes_estoque
```sql
CREATE TYPE tipo_movimento AS ENUM ('ENTRADA', 'SAIDA', 'AJUSTE', 'TRANSFERENCIA', 'VENDA', 'COMPRA', 'DEVOLUCAO');

CREATE TABLE movimentacoes_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL REFERENCES materiais(id) ON DELETE CASCADE,
    tipo_movimento tipo_movimento NOT NULL,
    quantidade INTEGER NOT NULL,
    custo_unitario DECIMAL(10,2), -- custo unitário na data da movimentação
    custo_total DECIMAL(10,2), -- quantidade * custo_unitario
    referencia_id UUID, -- ID da referência (venda, compra, etc.)
    tipo_referencia VARCHAR(100), -- tipo da referência
    motivo TEXT,
    observacoes TEXT,
    criado_por UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_movimentacoes_estoque_material_id ON movimentacoes_estoque(material_id);
CREATE INDEX idx_movimentacoes_estoque_tipo_movimento ON movimentacoes_estoque(tipo_movimento);
CREATE INDEX idx_movimentacoes_estoque_criado_em ON movimentacoes_estoque(criado_em);
```

### 5.4 Tabela pedidos_compra
```sql
CREATE TYPE status_pedido AS ENUM ('PENDENTE', 'APROVADO', 'PEDIDO', 'RECEBIDO', 'CANCELADO');

CREATE TABLE pedidos_compra (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    fornecedor_id UUID NOT NULL REFERENCES fornecedores(id) ON DELETE CASCADE,
    numero_pedido VARCHAR(100) UNIQUE NOT NULL,
    status status_pedido DEFAULT 'PENDENTE',
    data_pedido DATE NOT NULL,
    data_prevista_entrega DATE,
    data_entrega_real DATE,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    valor_impostos DECIMAL(10,2) NOT NULL DEFAULT 0,
    frete DECIMAL(10,2) NOT NULL DEFAULT 0,
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    condicoes_pagamento INTEGER DEFAULT 0,
    observacoes TEXT,
    criado_por UUID REFERENCES usuarios(id),
    aprovado_por UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_pedidos_compra_clinica_id ON pedidos_compra(clinica_id);
CREATE INDEX idx_pedidos_compra_fornecedor_id ON pedidos_compra(fornecedor_id);
CREATE INDEX idx_pedidos_compra_status ON pedidos_compra(status);
CREATE INDEX idx_pedidos_compra_data_pedido ON pedidos_compra(data_pedido);
```

### 5.5 Tabela itens_pedido_compra
```sql
CREATE TABLE itens_pedido_compra (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_compra_id UUID NOT NULL REFERENCES pedidos_compra(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES materiais(id) ON DELETE CASCADE,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    custo_unitario DECIMAL(10,2) NOT NULL CHECK (custo_unitario > 0),
    custo_total DECIMAL(10,2) NOT NULL,
    quantidade_recebida INTEGER DEFAULT 0,
    observacoes TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_itens_pedido_compra_pedido_id ON itens_pedido_compra(pedido_compra_id);
CREATE INDEX idx_itens_pedido_compra_material_id ON itens_pedido_compra(material_id);
```

## 6. Tabelas Financeiras

### 6.1 Tabela contas_financeiras
```sql
CREATE TABLE contas_financeiras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    tipo_conta VARCHAR(50) NOT NULL CHECK (tipo_conta IN ('DINHEIRO', 'BANCO', 'INVESTIMENTO', 'CARTAO_CREDITO', 'CARTEIRA_DIGITAL')),
    nome_banco VARCHAR(100),
    agencia VARCHAR(20),
    numero_conta VARCHAR(20),
    saldo_inicial DECIMAL(10,2) DEFAULT 0,
    saldo_atual DECIMAL(10,2) DEFAULT 0,
    limite_credito DECIMAL(10,2) DEFAULT 0,
    moeda VARCHAR(3) DEFAULT 'BRL',
    ativo BOOLEAN DEFAULT true,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_contas_financeiras_clinica_id ON contas_financeiras(clinica_id);
CREATE INDEX idx_contas_financeiras_tipo_conta ON contas_financeiras(tipo_conta);
```

### 6.2 Tabela transacoes_financeiras
```sql
CREATE TYPE tipo_transacao AS ENUM ('RECEITA', 'DESPESA', 'TRANSFERENCIA');
CREATE TYPE categoria_transacao AS ENUM ('SERVICO', 'MATERIAL', 'FOLHA_PAGAMENTO', 'ALUGUEL', 'UTILIDADES', 'MARKETING', 'OUTRO');
CREATE TYPE forma_pagamento AS ENUM ('DINHEIRO', 'CARTAO_DEBITO', 'CARTAO_CREDITO', 'TRANSFERENCIA_BANCARIA', 'PIX', 'CHEQUE', 'BOLETO');

CREATE TABLE transacoes_financeiras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    conta_id UUID NOT NULL REFERENCES contas_financeiras(id) ON DELETE CASCADE,
    tipo_transacao tipo_transacao NOT NULL,
    categoria_transacao categoria_transacao NOT NULL,
    forma_pagamento forma_pagamento,
    valor DECIMAL(10,2) NOT NULL CHECK (valor > 0),
    descricao TEXT NOT NULL,
    data_transacao DATE NOT NULL,
    data_vencimento DATE,
    data_pagamento DATE,
    referencia_id UUID, -- ID da referência (serviço, material, etc.)
    tipo_referencia VARCHAR(100), -- tipo da referência
    tags TEXT[], -- tags para categorização adicional
    anexos JSONB, -- URLs de anexos
    observacoes TEXT,
    criado_por UUID REFERENCES usuarios(id),
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_transacoes_financeiras_clinica_id ON transacoes_financeiras(clinica_id);
CREATE INDEX idx_transacoes_financeiras_conta_id ON transacoes_financeiras(conta_id);
CREATE INDEX idx_transacoes_financeiras_tipo ON transacoes_financeiras(tipo_transacao);
CREATE INDEX idx_transacoes_financeiras_categoria ON transacoes_financeiras(categoria_transacao);
CREATE INDEX idx_transacoes_financeiras_data_transacao ON transacoes_financeiras(data_transacao);
```

### 6.3 Tabela centros_custo
```sql
CREATE TABLE centros_custo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    pai_id UUID REFERENCES centros_custo(id) ON DELETE CASCADE,
    orcamento DECIMAL(10,2) DEFAULT 0,
    ativo BOOLEAN DEFAULT true,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    atualizado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(clinica_id, codigo)
);

CREATE INDEX idx_centros_custo_clinica_id ON centros_custo(clinica_id);
CREATE INDEX idx_centros_custo_pai_id ON centros_custo(pai_id);
```

## 7. Tabelas de Auditoria e Logs

### 7.1 Tabela logs_auditoria
```sql
CREATE TABLE logs_auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    clinica_id UUID NOT NULL REFERENCES clinicas(id) ON DELETE CASCADE,
    acao VARCHAR(100) NOT NULL,
    tipo_entidade VARCHAR(100) NOT NULL,
    entidade_id UUID,
    valores_antigos JSONB,
    valores_novos JSONB,
    alteracoes JSONB,
    endereco_ip INET,
    user_agent TEXT,
    sessao_id UUID,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_logs_auditoria_usuario_id ON logs_auditoria(usuario_id);
CREATE INDEX idx_logs_auditoria_clinica_id ON logs_auditoria(clinica_id);
CREATE INDEX idx_logs_auditoria_entidade ON logs_auditoria(tipo_entidade, entidade_id);
CREATE INDEX idx_logs_auditoria_criado_em ON logs_auditoria(criado_em);
```

### 7.2 Tabela logs_sistema
```sql
CREATE TABLE logs_sistema (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nivel VARCHAR(20) NOT NULL CHECK (nivel IN ('DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL')),
    logger VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    excecao TEXT,
    contexto JSONB,
    requisicao_id UUID,
    usuario_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    clinica_id UUID REFERENCES clinicas(id) ON DELETE CASCADE,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_logs_sistema_nivel ON logs_sistema(nivel);
CREATE INDEX idx_logs_sistema_logger ON logs_sistema(logger);
CREATE INDEX idx_logs_sistema_criado_em ON logs_sistema(criado_em);
CREATE INDEX idx_logs_sistema_clinica_id ON logs_sistema(clinica_id);
```

## 8. Views e Materialized Views

### 8.1 View resumo_precificacao_servico
```sql
CREATE OR REPLACE VIEW resumo_precificacao_servico AS
SELECT 
    s.id,
    s.clinica_id,
    s.nome,
    s.categoria,
    s.preco_sugerido,
    s.duracao,
    COALESCE(SUM(sm.quantidade * m.custo_unitario), 0) as custo_direto_material,
    COALESCE(SUM(sm.quantidade * m.custo_unitario * (1 + sm.percentual_perda/100)), 0) as custo_total_material,
    c.code as cnae_codigo,
    c.iss_rate,
    c.pis_rate,
    c.cofins_rate,
    c.irpj_rate,
    c.csll_rate,
    (c.iss_rate + c.pis_rate + c.cofins_rate + c.irpj_rate + c.csll_rate) as aliquota_total_impostos,
    s.ativo,
    s.criado_em
FROM servicos s
LEFT JOIN servico_materiais sm ON s.id = sm.servico_id
LEFT JOIN materiais m ON sm.material_id = m.id
LEFT JOIN cnae c ON s.cnae_id = c.id
GROUP BY s.id, s.clinica_id, s.nome, s.categoria, s.preco_sugerido, s.duracao, c.code, c.iss_rate, c.pis_rate, c.cofins_rate, c.irpj_rate, c.csll_rate, s.ativo, s.criado_em;
```

### 8.2 Materialized View situacao_estoque_material
```sql
CREATE MATERIALIZED VIEW situacao_estoque_material AS
SELECT 
    m.id,
    m.clinica_id,
    m.nome,
    m.estoque_atual,
    m.estoque_minimo,
    m.estoque_maximo,
    m.ponto_reposicao,
    CASE 
        WHEN m.estoque_atual <= 0 THEN 'SEM_ESTOQUE'
        WHEN m.estoque_atual <= m.estoque_minimo THEN 'ESTOQUE_BAIXO'
        WHEN m.estoque_atual >= m.estoque_maximo THEN 'ESTOQUE_EXCESSO'
        ELSE 'NORMAL'
    END as status_estoque,
    m.custo_unitario,
    (m.estoque_atual * m.custo_unitario) as valor_total,
    m.fornecedor_id,
    f.nome as nome_fornecedor,
    m.ativo,
    m.atualizado_em
FROM materiais m
LEFT JOIN fornecedores f ON m.fornecedor_id = f.id
WHERE m.ativo = true;

CREATE INDEX idx_situacao_estoque_material_clinica_id ON situacao_estoque_material(clinica_id);
CREATE INDEX idx_situacao_estoque_material_status_estoque ON situacao_estoque_material(status_estoque);

-- Atualização periódica
CREATE OR REPLACE FUNCTION atualizar_situacao_estoque_material()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY situacao_estoque_material;
END;
$$ LANGUAGE plpgsql;
```

### 8.3 View painel_financeiro
```sql
CREATE OR REPLACE VIEW painel_financeiro AS
WITH resumo_mensal AS (
    SELECT 
        clinica_id,
        DATE_TRUNC('month', data_transacao) as mes,
        SUM(CASE WHEN tipo_transacao = 'RECEITA' THEN valor ELSE 0 END) as total_receita,
        SUM(CASE WHEN tipo_transacao = 'DESPESA' THEN valor ELSE 0 END) as total_despesa,
        SUM(CASE WHEN tipo_transacao = 'RECEITA' THEN valor ELSE -valor END) as resultado_liquido
    FROM transacoes_financeiras
    WHERE data_transacao >= CURRENT_DATE - INTERVAL '12 months'
    GROUP BY clinica_id, DATE_TRUNC('month', data_transacao)
),
receita_servico AS (
    SELECT 
        clinica_id,
        DATE_TRUNC('month', data_pagamento) as mes,
        SUM(valor) as receita_servico
    FROM transacoes_financeiras
    WHERE categoria_transacao = 'SERVICO' 
    AND tipo_transacao = 'RECEITA'
    AND data_pagamento >= CURRENT_DATE - INTERVAL '12 months'
    GROUP BY clinica_id, DATE_TRUNC('month', data_pagamento)
),
custo_material AS (
    SELECT 
        clinica_id,
        DATE_TRUNC('month', data_pagamento) as mes,
        SUM(valor) as custo_material
    FROM transacoes_financeiras
    WHERE categoria_transacao = 'MATERIAL' 
    AND tipo_transacao = 'DESPESA'
    AND data_pagamento >= CURRENT_DATE - INTERVAL '12 months'
    GROUP BY clinica_id, DATE_TRUNC('month', data_pagamento)
)
SELECT 
    rm.clinica_id,
    rm.mes,
    rm.total_receita,
    rm.total_despesa,
    rm.resultado_liquido,
    COALESCE(rs.receita_servico, 0) as receita_servico,
    COALESCE(cm.custo_material, 0) as custo_material,
    (COALESCE(rs.receita_servico, 0) - COALESCE(cm.custo_material, 0)) as lucro_servico,
    CASE 
        WHEN COALESCE(rs.receita_servico, 0) > 0 
        THEN (COALESCE(rs.receita_servico, 0) - COALESCE(cm.custo_material, 0)) / COALESCE(rs.receita_servico, 0) * 100
        ELSE 0
    END as margem_lucro_servico
FROM resumo_mensal rm
LEFT JOIN receita_servico rs ON rm.clinica_id = rs.clinica_id AND rm.mes = rs.mes
LEFT JOIN custo_material cm ON rm.clinica_id = cm.clinica_id AND rm.mes = cm.mes
ORDER BY rm.clinica_id, rm.mes DESC;
```

## 9. Triggers e Procedures

### 9.1 Procedure para calcular custo do serviço
```sql
CREATE OR REPLACE FUNCTION calcular_custo_servico(
    p_servico_id UUID,
    p_clinica_id UUID
) RETURNS TABLE (
    servico_id UUID,
    custo_material_direto DECIMAL(10,2),
    percentual_custo_indireto DECIMAL(5,2),
    valor_custo_indireto DECIMAL(10,2),
    custo_total DECIMAL(10,2),
    preco_sugerido DECIMAL(10,2),
    margem_lucro DECIMAL(5,2)
) AS $$
DECLARE
    v_custo_direto DECIMAL(10,2) := 0;
    v_percentual_indireto DECIMAL(5,2) := 30.00; -- Padrão 30%
    v_valor_indireto DECIMAL(10,2) := 0;
    v_custo_total DECIMAL(10,2) := 0;
    v_preco_sugerido DECIMAL(10,2) := 0;
    v_margem_lucro DECIMAL(5,2) := 0;
BEGIN
    -- Calcular custo direto de material
    SELECT COALESCE(SUM(sm.quantidade * m.custo_unitario * (1 + sm.percentual_perda/100)), 0)
    INTO v_custo_direto
    FROM servico_materiais sm
    JOIN materiais m ON sm.material_id = m.id
    WHERE sm.servico_id = p_servico_id
    AND m.clinica_id = p_clinica_id
    AND m.ativo = true;
    
    -- Obter percentual de custo indireto da configuração da clínica
    SELECT COALESCE(aliquota_icms_personalizada, 30.00)
    INTO v_percentual_indireto
    FROM configuracao_cnae_clinica
    WHERE clinica_id = p_clinica_id
    AND ativo = true
    LIMIT 1;
    
    -- Calcular custo indireto
    v_valor_indireto := v_custo_direto * (v_percentual_indireto / 100);
    
    -- Calcular custo total
    v_custo_total := v_custo_direto + v_valor_indireto;
    
    -- Obter preço sugerido do serviço
    SELECT COALESCE(preco_sugerido, v_custo_total * 1.5)
    INTO v_preco_sugerido
    FROM servicos
    WHERE id = p_servico_id
    AND clinica_id = p_clinica_id
    AND ativo = true;
    
    -- Calcular margem de lucro
    IF v_preco_sugerido > 0 THEN
        v_margem_lucro := ((v_preco_sugerido - v_custo_total) / v_preco_sugerido) * 100;
    END IF;
    
    RETURN QUERY
    SELECT 
        p_servico_id,
        v_custo_direto,
        v_percentual_indireto,
        v_valor_indireto,
        v_custo_total,
        v_preco_sugerido,
        v_margem_lucro;
END;
$$ LANGUAGE plpgsql;
```

### 9.2 Procedure para gerar pedido de compra
```sql
CREATE OR REPLACE FUNCTION gerar_pedido_compra(
    p_clinica_id UUID,
    p_fornecedor_id UUID,
    p_solicitado_por UUID,
    p_observacoes TEXT DEFAULT NULL
) RETURNS TABLE (
    pedido_compra_id UUID,
    numero_pedido VARCHAR,
    valor_total DECIMAL(10,2),
    quantidade_itens INTEGER
) AS $$
DECLARE
    v_pedido_compra_id UUID;
    v_numero_pedido VARCHAR;
    v_valor_total DECIMAL(10,2) := 0;
    v_quantidade_itens INTEGER := 0;
BEGIN
    -- Gerar número do pedido
    v_numero_pedido := 'PC-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || 
                     (SELECT COALESCE(MAX(CAST(SUBSTRING(numero_pedido FROM 14) AS INTEGER)), 0) + 1
                      FROM pedidos_compra 
                      WHERE DATE(criado_em) = CURRENT_DATE);
    
    -- Criar pedido de compra
    INSERT INTO pedidos_compra (
        clinica_id,
        fornecedor_id,
        numero_pedido,
        status,
        valor_total,
        criado_por,
        observacoes,
        criado_em
    ) VALUES (
        p_clinica_id,
        p_fornecedor_id,
        v_numero_pedido,
        'PENDENTE',
        0,
        p_solicitado_por,
        p_observacoes,
        NOW()
    ) RETURNING id INTO v_pedido_compra_id;
    
    -- Adicionar materiais com estoque baixo automaticamente
    INSERT INTO itens_pedido_compra (
        pedido_compra_id,
        material_id,
        quantidade,
        custo_unitario,
        custo_total,
        criado_em
    )
    SELECT 
        v_pedido_compra_id,
        sem.id,
        CEIL(GREATEST(sem.ponto_reposicao - sem.estoque_atual, sem.estoque_minimo)),
        sem.custo_unitario,
        CEIL(GREATEST(sem.ponto_reposicao - sem.estoque_atual, sem.estoque_minimo)) * sem.custo_unitario,
        NOW()
    FROM situacao_estoque_material sem
    WHERE sem.clinica_id = p_clinica_id
    AND sem.status_estoque IN ('ESTOQUE_BAIXO', 'SEM_ESTOQUE')
    AND sem.fornecedor_id = p_fornecedor_id;
    
    -- Atualizar totais
    SELECT 
        COALESCE(SUM(custo_total), 0),
        COUNT(*)
    INTO v_valor_total, v_quantidade_itens
    FROM itens_pedido_compra
    WHERE pedido_compra_id = v_pedido_compra_id;
    
    -- Atualizar pedido de compra
    UPDATE pedidos_compra
    SET valor_total = v_valor_total
    WHERE id = v_pedido_compra_id;
    
    RETURN QUERY
    SELECT 
        v_pedido_compra_id,
        v_numero_pedido,
        v_valor_total,
        v_quantidade_itens;
END;
$$ LANGUAGE plpgsql;
```

### 9.3 Trigger para atualizar saldo_conta
```sql
CREATE OR REPLACE FUNCTION atualizar_saldo_conta()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE contas_financeiras 
        SET saldo_atual = saldo_atual + 
            CASE WHEN NEW.tipo_transacao = 'RECEITA' THEN NEW.valor 
                 WHEN NEW.tipo_transacao = 'DESPESA' THEN -NEW.valor 
                 ELSE 0 END
        WHERE id = NEW.conta_id;
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        -- Reverte o valor antigo
        UPDATE contas_financeiras 
        SET saldo_atual = saldo_atual - 
            CASE WHEN OLD.tipo_transacao = 'RECEITA' THEN OLD.valor 
                 WHEN OLD.tipo_transacao = 'DESPESA' THEN -OLD.valor 
                 ELSE 0 END
        WHERE id = OLD.conta_id;
        
        -- Aplica o novo valor
        UPDATE contas_financeiras 
        SET saldo_atual = saldo_atual + 
            CASE WHEN NEW.tipo_transacao = 'RECEITA' THEN NEW.valor 
                 WHEN NEW.tipo_transacao = 'DESPESA' THEN -NEW.valor 
                 ELSE 0 END
        WHERE id = NEW.conta_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE contas_financeiras 
        SET saldo_atual = saldo_atual - 
            CASE WHEN OLD.tipo_transacao = 'RECEITA' THEN OLD.valor 
                 WHEN OLD.tipo_transacao = 'DESPESA' THEN -OLD.valor 
                 ELSE 0 END
        WHERE id = OLD.conta_id;
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_atualizar_saldo_conta
    AFTER INSERT OR UPDATE OR DELETE ON transacoes_financeiras
    FOR EACH ROW EXECUTE FUNCTION atualizar_saldo_conta();
```

### 9.2 Trigger para auditoria
```sql
CREATE OR REPLACE FUNCTION funcao_auditoria_trigger()
RETURNS TRIGGER AS $$
DECLARE
    audit_data JSONB;
BEGIN
    IF TG_OP = 'UPDATE' THEN
        audit_data = jsonb_build_object(
            'tabela', TG_TABLE_NAME,
            'operacao', TG_OP,
            'valores_antigos', to_jsonb(OLD),
            'valores_novos', to_jsonb(NEW),
            'alteracoes', (to_jsonb(NEW) - to_jsonb(OLD))
        );
        
        INSERT INTO logs_auditoria (
            usuario_id, clinica_id, acao, tipo_entidade, entidade_id, 
            valores_antigos, valores_novos, alteracoes, criado_em
        ) VALUES (
            current_setting('app.current_user_id')::UUID,
            current_setting('app.current_clinic_id')::UUID,
            TG_OP,
            TG_TABLE_NAME,
            NEW.id,
            to_jsonb(OLD),
            to_jsonb(NEW),
            (to_jsonb(NEW) - to_jsonb(OLD)),
            NOW()
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger para tabelas importantes
CREATE TRIGGER auditoria_servicos
    AFTER UPDATE ON servicos
    FOR EACH ROW EXECUTE FUNCTION funcao_auditoria_trigger();

CREATE TRIGGER auditoria_materiais
    AFTER UPDATE ON materiais
    FOR EACH ROW EXECUTE FUNCTION funcao_auditoria_trigger();
```

## 10. Índices de Performance

### 10.1 Índices de performance
```sql
-- Índices de usuário
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_clinica_id ON usuarios(clinica_id);
CREATE INDEX idx_usuarios_criado_em ON usuarios(criado_em);

-- Índices de serviço
CREATE INDEX idx_servicos_clinica_id ON servicos(clinica_id);
CREATE INDEX idx_servicos_categoria ON servicos(categoria);
CREATE INDEX idx_servicos_status ON servicos(ativo);
CREATE INDEX idx_servicos_criado_em ON servicos(criado_em);

-- Índices de material
CREATE INDEX idx_materiais_clinica_id ON materiais(clinica_id);
CREATE INDEX idx_materiais_sku ON materiais(sku);
CREATE INDEX idx_materiais_fornecedor_id ON materiais(fornecedor_id);
CREATE INDEX idx_materiais_estoque_atual ON materiais(estoque_atual);
CREATE INDEX idx_materiais_status_estoque ON materiais(estoque_atual, estoque_minimo);

-- Índices financeiros
CREATE INDEX idx_transacoes_financeiras_clinica_id ON transacoes_financeiras(clinica_id);
CREATE INDEX idx_transacoes_financeiras_conta_id ON transacoes_financeiras(conta_id);
CREATE INDEX idx_transacoes_financeiras_data_transacao ON transacoes_financeiras(data_transacao);
CREATE INDEX idx_transacoes_financeiras_tipo ON transacoes_financeiras(tipo_transacao);
CREATE INDEX idx_transacoes_financeiras_categoria ON transacoes_financeiras(categoria_transacao);

-- Índices de pedidos de compra
CREATE INDEX idx_pedidos_compra_clinica_id ON pedidos_compra(clinica_id);
CREATE INDEX idx_pedidos_compra_fornecedor_id ON pedidos_compra(fornecedor_id);
CREATE INDEX idx_pedidos_compra_status ON pedidos_compra(status);
CREATE INDEX idx_pedidos_compra_criado_em ON pedidos_compra(criado_em);

-- Índices de movimentação de estoque
CREATE INDEX idx_movimentacoes_estoque_clinica_id ON movimentacoes_estoque(material_id);
CREATE INDEX idx_movimentacoes_estoque_material_id ON movimentacoes_estoque(material_id);
CREATE INDEX idx_movimentacoes_estoque_tipo ON movimentacoes_estoque(tipo_movimento);
CREATE INDEX idx_movimentacoes_estoque_criado_em ON movimentacoes_estoque(criado_em);
```

### 10.2 Índices compostos para consultas complexas
```sql
-- Análise de precificação de serviços
CREATE INDEX idx_servicos_precificacao ON servicos(clinica_id, categoria, preco_sugerido, preco_final);

-- Gerenciamento de estoque de materiais
CREATE INDEX idx_materiais_gestao_estoque ON materiais(clinica_id, estoque_atual, estoque_minimo, estoque_maximo, fornecedor_id);

-- Análise financeira por período
CREATE INDEX idx_transacoes_financeiras_periodo ON transacoes_financeiras(clinica_id, data_transacao, tipo_transacao, categoria_transacao);

-- Análise de pedidos de compra
CREATE INDEX idx_pedidos_compra_analise ON pedidos_compra(clinica_id, fornecedor_id, status, valor_total, criado_em);

-- Rastreamento de movimentação de estoque
CREATE INDEX idx_movimentacoes_estoque_rastreamento ON movimentacoes_estoque(clinica_id, material_id, tipo_movimento, criado_em, quantidade);
```

## 14. Backup e Segurança

### 14.1 Estratégia de backup
```sql
-- Criar usuário de backup com permissões limitadas
CREATE USER usuario_backup WITH PASSWORD 'senha_backup_segura';
GRANT CONNECT ON DATABASE finesse_db TO usuario_backup;
GRANT USAGE ON SCHEMA public TO usuario_backup;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO usuario_backup;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO usuario_backup;

-- Criar função de monitoramento de backup
CREATE OR REPLACE FUNCTION monitorar_status_backup()
RETURNS TABLE (
    data_backup DATE,
    tamanho_backup BIGINT,
    status_backup VARCHAR(20),
    idade_ultimo_backup INTERVAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        sb.data_backup,
        sb.tamanho_backup,
        sb.status_backup,
        CURRENT_DATE - sb.data_backup as idade_ultimo_backup
    FROM status_backup sb
    WHERE sb.data_backup >= CURRENT_DATE - INTERVAL '7 days'
    ORDER BY sb.data_backup DESC;
END;
$$ LANGUAGE plpgsql;
```

### 14.2 Configurações de segurança
```sql
-- Habilitar segurança em nível de linha nas tabelas sensíveis
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE transacoes_financeiras ENABLE ROW LEVEL SECURITY;
ALTER TABLE logs_auditoria ENABLE ROW LEVEL SECURITY;

-- Criar políticas RLS para isolamento de clínica
CREATE POLICY isolamento_clinica ON usuarios
    FOR ALL
    USING (clinica_id = current_setting('app.current_clinic_id')::UUID);

CREATE POLICY isolamento_clinica ON transacoes_financeiras
    FOR ALL
    USING (clinica_id = current_setting('app.current_clinic_id')::UUID);

CREATE POLICY isolamento_clinica ON logs_auditoria
    FOR ALL
    USING (clinica_id = current_setting('app.current_clinic_id')::UUID);

-- Criar políticas específicas por usuário
CREATE POLICY acesso_dados_usuario ON usuarios
    FOR SELECT
    USING (id = current_setting('app.current_user_id')::UUID OR 
           current_setting('app.user_role')::TEXT = 'admin');

-- Conceder permissões apropriadas
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO authenticated;
GRANT SELECT ON usuarios, servicos, materiais TO anon;
```

### 14.3 Criptografia de dados
```sql
-- Criar extensão para criptografia
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Criar função para criptografar dados sensíveis
CREATE OR REPLACE FUNCTION criptografar_dados_sensiveis(
    p_dados TEXT,
    p_chave TEXT
) RETURNS TEXT AS $$
BEGIN
    RETURN encode(pgp_sym_encrypt(p_dados, p_chave), 'base64');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Criar função para descriptografar dados sensíveis
CREATE OR REPLACE FUNCTION descriptografar_dados_sensiveis(
    p_dados_criptografados TEXT,
    p_chave TEXT
) RETURNS TEXT AS $$
BEGIN
    RETURN pgp_sym_decrypt(decode(p_dados_criptografados, 'base64'), p_chave);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Exemplo de uso em tabela com campos criptografados
CREATE TABLE dados_sensiveis_criptografados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    clinica_id UUID NOT NULL,
    tipo_dado VARCHAR(50) NOT NULL,
    dados_criptografados TEXT NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    FOREIGN KEY (clinica_id) REFERENCES clinicas(id)
);
```

-- Criar partições para o ano atual
CREATE TABLE transacoes_financeiras_2024_q1 PARTITION OF transacoes_financeiras_particionadas
    FOR VALUES FROM ('2024-01-01') TO ('2024-04-01');

CREATE TABLE transacoes_financeiras_2024_q2 PARTITION OF transacoes_financeiras_particionadas
    FOR VALUES FROM ('2024-04-01') TO ('2024-07-01');

CREATE TABLE transacoes_financeiras_2024_q3 PARTITION OF transacoes_financeiras_particionadas
    FOR VALUES FROM ('2024-07-01') TO ('2024-10-01');

CREATE TABLE transacoes_financeiras_2024_q4 PARTITION OF transacoes_financeiras_particionadas
    FOR VALUES FROM ('2024-10-01') TO ('2025-01-01');
```

### 12.2 Particionamento de logs de auditoria
```sql
-- Criar tabela particionada para logs de auditoria
CREATE TABLE logs_auditoria_particionados (
    id UUID DEFAULT gen_random_uuid(),
    usuario_id UUID,
    clinica_id UUID NOT NULL,
    acao VARCHAR(100) NOT NULL,
    tipo_entidade VARCHAR(50) NOT NULL,
    entidade_id UUID NOT NULL,
    nome_tabela VARCHAR(50),
    registro_id UUID,
    valores_antigos JSONB,
    valores_novos JSONB,
    alteracoes JSONB,
    endereco_ip INET,
    user_agent TEXT,
    criado_em TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (id, criado_em)
) PARTITION BY RANGE (criado_em);

-- Criar partições mensais para logs de auditoria
CREATE TABLE logs_auditoria_2024_01 PARTITION OF logs_auditoria_particionados
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE logs_auditoria_2024_02 PARTITION OF logs_auditoria_particionados
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

CREATE TABLE logs_auditoria_2024_03 PARTITION OF logs_auditoria_particionados
    FOR VALUES FROM ('2024-03-01') TO ('2024-04-01');

-- Continuar padrão para meses adicionais...
```

### 12.3 Gerenciamento automático de partições
```sql
-- Função para criar partições mensais automaticamente
CREATE OR REPLACE FUNCTION criar_particoes_mensais(
    p_nome_tabela TEXT,
    p_data_inicio DATE,
    p_meses_a_frente INTEGER DEFAULT 3
) RETURNS VOID AS $$
DECLARE
    v_nome_particao TEXT;
    v_data_inicio DATE;
    v_data_fim DATE;
    v_data_atual DATE := p_data_inicio;
BEGIN
    FOR i IN 0..p_meses_a_frente-1 LOOP
        v_data_inicio := DATE_TRUNC('month', v_data_atual + (i || ' months')::INTERVAL);
        v_data_fim := v_data_inicio + '1 month'::INTERVAL;
        v_nome_particao := p_nome_tabela || '_' || TO_CHAR(v_data_inicio, 'YYYY_MM');
        
        EXECUTE format('
            CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
            FOR VALUES FROM (%L) TO (%L);
        ', v_nome_particao, p_nome_tabela, v_data_inicio, v_data_fim);
        
        RAISE NOTICE 'Partição criada: %', v_nome_particao;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Criar partições para os próximos 3 meses
SELECT criar_particoes_mensais('logs_auditoria_particionados', CURRENT_DATE, 3);
SELECT criar_particoes_mensais('transacoes_financeiras_particionadas', CURRENT_DATE, 3);
```

## 13. Monitoramento e Manutenção

### 13.1 Queries de monitoramento
```sql
-- Tamanho das tabelas
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Índices não utilizados
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_tup_read,
    idx_tup_fetch,
    pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
FROM pg_stat_user_indexes 
WHERE idx_tup_read = 0 AND idx_tup_fetch = 0
ORDER BY pg_relation_size(indexrelid) DESC;

-- Queries lentas
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    max_time,
    rows
FROM pg_stat_statements
WHERE mean_time > 100
ORDER BY mean_time DESC
LIMIT 10;
```

### 13.2 Manutenção regular
```sql
-- Análise e vacuum das tabelas
VACUUM ANALYZE;

-- Reindexação de índices fragmentados
REINDEX DATABASE finesse_clinic;

-- Atualização de estatísticas
SELECT pg_stat_reset();
```

### 13.3 Queries de inteligência de negócios
```sql
-- Resumo financeiro mensal
SELECT 
    DATE_TRUNC('month', data_transacao) as mes,
    COUNT(*) as quantidade_transacoes,
    SUM(CASE WHEN tipo_transacao = 'RECEITA' THEN valor ELSE 0 END) as total_receita,
    SUM(CASE WHEN tipo_transacao = 'DESPESA' THEN valor ELSE 0 END) as total_despesa,
    SUM(CASE WHEN tipo_transacao = 'RECEITA' THEN valor ELSE -valor END) as lucro_liquido
FROM transacoes_financeiras
WHERE data_transacao >= CURRENT_DATE - INTERVAL '12 months'
    AND status = 'concluido'
GROUP BY DATE_TRUNC('month', data_transacao)
ORDER BY mes DESC;

-- Resumo de alertas de estoque
SELECT 
    sem.status_estoque,
    COUNT(*) as quantidade_material,
    SUM(sem.valor_total) as valor_total
FROM situacao_estoque_material sem
WHERE sem.clinica_id = current_setting('app.current_clinic_id')::UUID
GROUP BY sem.status_estoque;

-- Análise de rentabilidade de serviços
SELECT 
    s.categoria,
    COUNT(*) as quantidade_servico,
    AVG(s.preco_sugerido) as preco_sugerido_medio,
    AVG(csc.custo_total) as custo_total_medio,
    AVG(s.preco_sugerido - csc.custo_total) as lucro_medio,
    AVG((s.preco_sugerido - csc.custo_total) / s.preco_sugerido * 100) as margem_lucro_media
FROM servicos s
CROSS JOIN LATERAL calcular_custo_servico(s.id, s.clinica_id) csc
WHERE s.ativo = true
GROUP BY s.categoria;
```