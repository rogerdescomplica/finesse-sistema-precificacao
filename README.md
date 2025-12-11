# Finesse App - Banco de Dados PostgreSQL em Docker

## Requisitos
- Docker Desktop (Windows/macOS) ou Docker Engine (Linux)
- Porta `5432` livre no host

## Compose
O projeto inclui um `docker-compose.yml` com:
- `postgres` (imagem oficial `postgres:16-alpine`), com variáveis `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, porta `5432`, volume persistente e init script `init.sql`
- `pgadmin` para administração via UI (`http://localhost:8081`)
- `postgres-backup` com backups diários (`pg_dump`) para `/backups`

### Inicialização
```sh
docker compose up -d
```
- Reinicia automaticamente (`restart: unless-stopped`)
- Healthcheck garante que dependentes iniciem após o banco estar pronto

### Parar/Remover
```sh
docker compose down
```
Para preservar dados, os volumes permanecem. Para limpar:
```sh
docker compose down -v
```

## Conexão da Aplicação
- Backend Spring Boot aponta para `jdbc:postgresql://localhost:5432/finesse_db`
- Credenciais padrão: `finesse_user` / `finesse_password`
- Configuração em `backend/src/main/resources/application.yml`
- Pool Hikari ajustado (`maximum-pool-size: 10`, timeouts)

## Admin Rápido
- pgAdmin: `http://localhost:8081` (usuario: `admin@finesse.com`, senha: `admin_password`)
- CLI (exemplo):
```sh
docker exec -it finesse-postgres psql -U finesse_user -d finesse_db
```

## Backups
- Serviço `postgres-backup` grava dumps em volume `postgres_backups`
- Cron: `0 2 * * *` (02:00 diariamente)
- Para recuperar um backup: copie o arquivo `.sql` e restaure via `psql` ou pgAdmin

## Scripts de Inicialização
- `init.sql` em raiz é montado como `/docker-entrypoint-init.sql`
- Adicione seus scripts de criação de esquemas/roles neste arquivo (ou mapeie uma pasta para `/docker-entrypoint-initdb.d`)

## Dicas
- Em produção, configure variáveis via `.env` e não commite credenciais
- Restrinja CORS e use TLS para conexões externas
- Ajuste tamanho do pool conforme carga e recursos