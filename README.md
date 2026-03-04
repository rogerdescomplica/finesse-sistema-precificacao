# Sistema de Precificação – Finesse Centro Integrado

Sistema desenvolvido para cálculo inteligente de preços de serviços de estética e saúde, considerando custos reais da clínica, impostos, custo fixo e margem de lucro.

Projeto aplicado na **Finesse Centro Integrado – Florianópolis**, responsável técnica Dra. Thayse Vieira.

---

## Problema real da clínica

Na área de estética e saúde, muitos preços são definidos sem considerar:

* custo real de insumos
* tempo da profissional
* impostos
* custo fixo da clínica
* margem de lucro desejada

Isso gera prejuízo sem perceber.

👉 Este sistema resolve isso com cálculo automático.

---

## Metodologia de Precificação

Cálculo baseado em custo direto + impostos + custo fixo + margem de lucro.

```
custo direto = mão de obra + insumos
imposto % = alíquota atividade
custo fixo % = média das receitas / média das despesas
lucro líquido = vendaAtual × (1 − imposto − custo fixo) − custoDireto
```

### Preço sugerido

```
markup = 1 ÷ (1 − (impostos + custo fixo + margem))
preço sugerido = custo direto × markup
```

Aplicado em serviços reais como:

* Preenchimento labial
* Bioestimulador
* Limpeza de pele
* Drenagem linfática
* Microagulhamento
....
---

##  Stack Tecnológica

**Backend**

* Java Spring Boot
* PostgreSQL
* JPA / Hibernate

**Frontend**

* SvelteKit + Tailwind
* Integração REST API

**Infra**

* Docker Compose
* pgAdmin

---

## Funcionalidades

✔ Cadastro de serviços
✔ Controle de materiais
✔ Cálculo automático de markup
✔ Simulação de preço praticado
✔ Lucro líquido por serviço
✔ Ajuste de alíquota
✔ Relatórios para gestão da clínica

---

## Projeto Acadêmico

Curso: Análise e Desenvolvimento de Sistemas
Projeto de Extensão (PEX V)

Sistema aplicado em empresa real: **Finesse Centro Integrado de Saúde, Beleza e Bem Estar**.

---

## Como rodar o projeto

Pré‑requisitos

- Docker Desktop instalado e em execução.
- Node.js LTS instalado (npm, pnpm ou yarn).
- Porta 5432 livre (PostgreSQL) e 8080 livre (backend).
- Porta 5173 livre (Vite dev) e 4173 para preview.

Com Docker (recomendado)

- No diretório raiz do projeto:
  - docker compose up -d
- Isso sobe:
  - Postgres em localhost:5432
  - Backend Spring Boot em http://localhost:8080
  - pgAdmin em http://localhost:8081 (login: admin@finesse.com / admin_password)
- Suba o frontend:
  - cd frontend
  - npm install
  - Crie o arquivo .env com: BACKEND_URL= http://localhost:8080
  - npm run dev
- Abra o app em http://localhost:5173

---

## Estrutura do Projeto

```
backend/
frontend/
docker-compose.yml
init.sql
```

---

# Banco de Dados PostgreSQL em Docker

