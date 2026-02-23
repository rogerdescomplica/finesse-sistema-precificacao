-- =========================
-- SEED (DEMO)
-- =========================

-- Usuário admin (evita duplicar pelo email)
INSERT INTO public.usuarios (nome, email, senha, perfil, ativo)
SELECT
  'Administrador',
  'admin@sistema.com',
  '$2a$10$es.zMEVd7ASaVWWXiKaceOnX7hoEm3wHgIL9d5UjgcfPDpGxqSQtK',
  'ADMIN',
  true
WHERE NOT EXISTS (
  SELECT 1 FROM public.usuarios WHERE email = 'admin@sistema.com'
);

-- Configuração ativa (garante só 1 ativa)
INSERT INTO public.configuracoes (
  pretensao_salarial_mensal,
  horas_semanais,
  semanas_media_mes,
  custo_fixo_pct,
  margem_lucro_padrao_pct,
  atualizado_em,
  ativo
)
SELECT
  12000.00,
  50.00,
  4.33,
  12.0000,
  20.0000,
  CURRENT_TIMESTAMP,
  TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM public.configuracoes WHERE ativo IS TRUE
);

-- Atividades
INSERT INTO public.atividades (nome, cnae, aliquota_total_pct, iss_pct, observacao, ativo)
SELECT * FROM (VALUES
  ('Fisioterapia', '8650-0/04', 16.7700, 2.0000, 'Exemplo para serviços de fisioterapia.', TRUE),
  ('Estética / Tratamento de pele / Depilação e congêneres', '9602-5/02', 8.6300, 2.0000, 'Exemplo para serviços estéticos.', TRUE),
  ('Atividades físicas (Pilates / Treinamento)', '9313-1/00', 16.7700, 2.0000, 'Exemplo para atividades físicas.', TRUE)
) AS v(nome, cnae, aliquota_total_pct, iss_pct, observacao, ativo)
WHERE NOT EXISTS (
  SELECT 1 FROM public.atividades a WHERE a.nome = v.nome
);

-- Materiais (protege por produto)
INSERT INTO public.materiais (produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
SELECT * FROM (VALUES
  ('Sabonete Facial Líquido', 'ml', 500.00, 85.00, 'Frasco de 500ml', true),
  ('Tônico Facial', 'ml', 500.00, 95.00, 'Frasco de 500ml', true),
  ('Esfoliante Facial', 'g', 500.00, 120.00, 'Pote de 500g', true),
  ('Máscara de Argila Verde', 'g', 500.00, 78.00, 'Pote de 500g', true),
  ('Máscara de Argila Branca', 'g', 500.00, 82.00, 'Pote de 500g', true),

  ('Sérum Vitamina C', 'ml', 30.00, 145.00, 'Frasco de 30ml', true),
  ('Sérum Ácido Hialurônico', 'ml', 30.00, 165.00, 'Frasco de 30ml', true),
  ('Creme Anti-idade', 'g', 50.00, 185.00, 'Pote de 50g', true),
  ('Creme Hidratante Facial', 'g', 200.00, 125.00, 'Pote de 200g', true),
  ('Gel Clareador', 'g', 30.00, 98.00, 'Bisnaga de 30g', true),
  ('Ampola Ácido Hialurônico', 'ml', 60.00, 220.00, 'Caixa com 12 ampolas de 5ml', true),

  ('Creme de Massagem Neutro', 'kg', 1.00, 68.00, 'Pote de 1kg', true),
  ('Gel Redutor', 'kg', 1.00, 145.00, 'Pote de 1kg', true),
  ('Gel Crioterápico', 'kg', 1.00, 125.00, 'Pote de 1kg', true),
  ('Loção Hidratante Corporal', 'l', 1.00, 65.00, 'Frasco de 1 litro', true),

  ('Lençol Descartável', 'm', 50.00, 85.00, 'Rolo com 50 metros', true),
  ('Touca Descartável', 'un', 100.00, 28.00, 'Pacote com 100 unidades', true),
  ('Máscara Descartável', 'un', 50.00, 45.00, 'Caixa com 50 unidades', true),
  ('Luvas de Procedimento', 'un', 100.00, 42.00, 'Caixa com 100 unidades', true),
  ('Algodão em Disco', 'un', 100.00, 18.00, 'Pacote com 100 discos', true),
  ('Gaze Estéril', 'un', 100.00, 55.00, 'Caixa com 100 unidades', true),
  ('Espátula Descartável', 'un', 100.00, 35.00, 'Pacote com 100 unidades', true),
  ('Toalha de Rosto Branca', 'un', 12.00, 180.00, 'Pacote com 12 toalhas', true),

  ('Gel para Ultrassom', 'kg', 1.00, 68.00, 'Pote de 1kg', true),
  ('Gel Condutor Radiofrequência', 'kg', 1.00, 125.00, 'Pote de 1kg', true),
  ('Eletrodos Descartáveis', 'un', 50.00, 95.00, 'Pacote com 50 pares', true),
  ('Lâminas Dermaplaning', 'un', 10.00, 85.00, 'Caixa com 10 lâminas estéreis', true),
  ('Agulhas para Microagulhamento', 'un', 12.00, 180.00, 'Caixa com 12 cartuchos estéreis', true),

  ('Máscara Hidratante Facial', 'un', 10.00, 120.00, 'Caixa com 10 máscaras de tecido', true),
  ('Máscara de Colágeno', 'un', 10.00, 145.00, 'Caixa com 10 máscaras', true),
  ('Água Termal', 'ml', 150.00, 58.00, 'Spray de 150ml', true)
) AS v(produto, unidade_medida, volume_embalagem, preco_embalagem, observacoes, ativo)
WHERE NOT EXISTS (
  SELECT 1 FROM public.materiais m WHERE m.produto = v.produto
);

-- Serviços (amarrados às atividades pelo nome)
INSERT INTO public.servicos (nome, grupo, duracao_minutos, atividade_id, margem_lucro_custom_pct, ativo)
SELECT * FROM (VALUES
  ('Drenagem Linfática Manual', 'Corporal', 60, 'Fisioterapia', NULL, TRUE),
  ('Limpeza de Pele', 'Facial', 60, 'Estética / Tratamento de pele / Depilação e congêneres', 25.0000, TRUE),
  ('Microagulhamento', 'Facial', 60, 'Estética / Tratamento de pele / Depilação e congêneres', 30.0000, TRUE),
  ('Pilates (sessão)', 'Pilates', 50, 'Atividades físicas (Pilates / Treinamento)', NULL, TRUE)
) AS v(nome, grupo, duracao_minutos, atividade_nome, margem_lucro_custom_pct, ativo)
WHERE NOT EXISTS (
  SELECT 1 FROM public.servicos s WHERE s.nome = v.nome
)
RETURNING 1;

-- Inserir serviços usando lookup de atividade_id (2ª etapa mais segura)
-- (alternativa: fazer tudo numa CTE; mantive simples)
UPDATE public.servicos s
SET atividade_id = a.id
FROM public.atividades a
WHERE s.atividade_id IS NULL
  AND (
    (s.nome = 'Drenagem Linfática Manual' AND a.nome = 'Fisioterapia')
    OR (s.nome IN ('Limpeza de Pele','Microagulhamento') AND a.nome = 'Estética / Tratamento de pele / Depilação e congêneres')
    OR (s.nome = 'Pilates (sessão)' AND a.nome = 'Atividades físicas (Pilates / Treinamento)')
  );