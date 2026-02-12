# Metodologia de Lucro Unificado

## Conceito
Lucro líquido do serviço é calculado com base na área de Serviços:
- custo direto = mão de obra + insumos
- imposto (R$) = venda praticada × alíquota total da atividade
- Custo fixo (R$) = custo direto × custo fixo (% configuração)
- lucro líquido (R$) = venda praticada − imposto − fixo − custo direto
- margem (%) = lucro líquido ÷ venda praticada

## Parâmetros
- Alíquota: atividade.aliquotaTotalPct (fração)
- Fixo: configuração.custoFixoPct (fração)
- Margem alvo: configuração.margemLucroPadraoPct ou margem customizada do serviço

## Preço sugerido
- markup = 1 ÷ (1 − (alíquota + fixo + margem alvo))
- venda sugerida = custo direto × markup

## Integração
- Listagem de Serviços: coluna Valor usa preço vigente (preco-praticado).
- Precificação: usa lucro líquido e margem (%) sobre venda; custo direto alinhado.

## Validação Cruzada
- Comparar venda atual versus sugerida; status:
  - PREJUÍZO: lucro líquido < 0
  - ABAIXO: margem (%) < margem alvo
  - SAUDÁVEL: demais casos

## Observações
- Todas as telas usam a mesma fonte de preço vigente e as mesmas fórmulas acima.
