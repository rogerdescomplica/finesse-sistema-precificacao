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

### Banco de Dados (Docker)

```bash
docker compose up -d
```

pgAdmin → http://localhost:8081

---

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

---

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## Estrutura do Projeto

```
backend/
frontend/
docker-compose.yml
init.sql
docs/
```

---

# Banco de Dados PostgreSQL em Docker

