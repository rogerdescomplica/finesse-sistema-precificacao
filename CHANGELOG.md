# Changelog

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Removido
- **Módulo Financeiro Completo**: Removidas todas as funcionalidades relacionadas à gestão financeira
  - Entidades: `ContaFinanceira` e `TransacaoFinanceira`
  - Repositórios: `ContaFinanceiraRepository` e `TransacaoFinanceiraRepository`
  - Controlador: `TransacaoFinanceiraController` com todos os endpoints de transações
  - DTO: `ResumoDiarioDTO` e pacote `com.finesse.dto.financeiro`
  - Testes: `TransacaoFinanceiraControllerTest`
  
  A remoção inclui:
  - Gestão de contas bancárias e financeiras
  - Registro de transações financeiras (receitas/despesas)
  - Relatórios financeiros (resumo mensal, diário, despesas fixas)
  - Integração entre transações e contas financeiras
  
  **Motivo**: Simplificação do escopo do sistema para focar nas funcionalidades principais de gestão de serviços, materiais e clientes da clínica de estética.

### Impacto
- A entidade `Clinica` manteve os campos `despesasFixasMensais` e `custoHoraTrabalho` que são utilizados para cálculos de custos de serviços
- Nenhuma outra funcionalidade foi afetada pela remoção do módulo financeiro
- O sistema continua operacional com todas as demais funcionalidades intactas

## [1.0.0] - 2025-11-16

### Adicionado
- Sistema completo de gestão de clínica de estética
- Autenticação e autorização com JWT
- Gestão de usuários e clínicas
- Cadastro de materiais e serviços
- Cálculo automático de custos de serviços
- API RESTful documentada com Swagger/OpenAPI
- Frontend em SvelteKit com interface moderna e responsiva