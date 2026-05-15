# VemResolver — Marketplace de Serviços

Plataforma web que conecta clientes a prestadores de serviços autônomos. O fluxo central é: cliente abre uma solicitação → prestadores enviam propostas → cliente aceita → serviço é executado → cliente e prestador avaliam-se mutuamente.

---

## Stack tecnológica

**Backend**
- Java 21
- Spring Boot 3.2.1
- Spring Security + JJWT 0.12.3 (autenticação JWT stateless)
- Spring Data JPA + Hibernate
- PostgreSQL 16
- Springdoc OpenAPI 2.3.0 (Swagger)
- Maven 3.9+

**Frontend**
- Angular 19.2
- TypeScript 5.7
- Tailwind CSS 3.4
- Angular Material 19.2
- RxJS 7.8

**Infraestrutura**
- Docker + Docker Compose (ambiente completo em um comando)

---

## Subindo o ambiente com Docker (recomendado)

```bash
docker-compose up
```

Aguarde até ver no log:

```
marketplace-backend  | Started MarketplaceServicosApplication in XX seconds
marketplace-frontend |   ➜  Local: http://localhost:4200/
```

- Frontend: `http://localhost:4200`
- API / Swagger: `http://localhost:8080/swagger-ui.html`

Para parar e preservar os dados:
```bash
docker-compose down
```

Para parar e apagar os dados do banco:
```bash
docker-compose down -v
```

---

## Subindo manualmente (sem Docker)

### Backend

1. Crie o banco:
```sql
CREATE DATABASE marketplace_db;
```

2. Crie `backend/.env.dev`:
```env
DB_URL=jdbc:postgresql://localhost:5432/marketplace_db
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_aqui
JWT_SECRET=bWFya2V0cGxhY2UtZGV2LXNlY3JldC1rZXktbWluaW11bS0yNTZiaXRzLWxlbmd0aC1vaw==
JWT_EXPIRATION=86400000
```

3. Execute:
```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend/marketplace-frontend
npm install
npm start
```

---

## Usuários de teste

Criados automaticamente pelo `DataSeeder` apenas nos perfis `dev` / padrão (nunca em `prod`):

| Papel | Email | Senha |
|-------|-------|-------|
| Cliente | `joao@cliente.com` | `123456` |
| Prestador | `maria@prestador.com` | `123456` |
| Admin | `admin@vemresolver.com` | `Admin@2025` |

---

## Documentação técnica

| Documento | Descrição |
|-----------|-----------|
| [`docs/DAS.md`](docs/DAS.md) | Arquitetura de Software — camadas, pacotes, modelo de dados |
| [`docs/REGRAS-DE-NEGOCIO.md`](docs/REGRAS-DE-NEGOCIO.md) | Fluxo completo do marketplace, estados, autorizações |
| [`docs/API.md`](docs/API.md) | Contratos OpenAPI dos endpoints principais |
| [`docs/LEVANTAMENTO.md`](docs/LEVANTAMENTO.md) | Levantamento técnico — estado atual, débitos e plano de ação |

---

## Estrutura do repositório

```
vemresolver/
├── backend/                    # Spring Boot
│   ├── src/main/java/com/marketplace/servicos/
│   └── src/main/resources/application.yml
├── frontend/
│   └── marketplace-frontend/   # Angular 19
│       └── src/app/
├── docs/                       # Documentação técnica
├── docker-compose.yml
└── README.md
```

---

## O que está feito

### Fluxo central do marketplace

- ✅ Cadastro e login com verificação de e-mail (JWT 24h)
- ✅ Guards de rota por papel (cliente / prestador)
- ✅ Criação de solicitação de serviço (cliente)
- ✅ Listagem de solicitações abertas para prestadores
- ✅ Envio de proposta com valor e mensagem (prestador)
- ✅ Propostas recebidas — aceitar / recusar com confirmação (cliente)
- ✅ Painel do prestador com contratos em andamento, propostas pendentes e estatísticas reais
- ✅ Marcar serviço como concluído — endpoint dedicado `/concluir` com validação de ownership
- ✅ Avaliação do prestador pelo cliente — estrelas 1–5, comentário, estados completos
- ✅ Avaliação bidirecional — prestador avalia cliente; notas médias exibidas nos perfis
- ✅ Perfil do prestador — criar/editar com pré-preenchimento automático via `GET /api/prestadores/me`
- ✅ Perfil do cliente com aba de avaliações recebidas e nota média
- ✅ Soft delete de conta
- ✅ Botão "Editar" no dashboard do cliente fecha o modal e navega corretamente para a tela de edição
- ✅ Campo `dataCriacao` na entidade `Avaliacao` — avaliações agora têm timestamp de criação

### Segurança e infraestrutura

- ✅ CORS via variável de ambiente `${FRONTEND_URL}` — sem hardcode
- ✅ URLs da API Angular centralizadas em `environment.ts` (`/api` em prod, `localhost:8080` em dev)
- ✅ Interceptor Angular de 401 — limpa `localStorage` e redireciona para `/login`
- ✅ Swagger desabilitado em produção (`application-prod.yml`)
- ✅ `DataSeeder` restrito ao perfil `!prod` — sem usuários de teste em produção
- ✅ Ownership em aceitar/recusar propostas e em alterar status de solicitação
- ✅ Endpoints de categoria (`POST/PUT/DELETE`) restritos a `ROLE_ADMIN` — via `SecurityFilterChain` + `@PreAuthorize`
- ✅ Criação de perfil de prestador restrita a `ROLE_PRESTADOR`
- ✅ `UserController` legado removido — substituído por `PerfilController` com DTOs seguros (`UpdatePerfilDTO`)
- ✅ Rodapé padronizado em todas as páginas públicas (logo + links + copyright)
- ✅ `PropostaController` desacoplado do repositório — resolução de prestador movida para `PropostaService`

---

## O que fazer a seguir

### Prioridade 1 — Qualidade e segurança (recomendado antes de produção)

| # | Tarefa | Área | Esforço |
|---|--------|------|---------|
| M4 | Implementar refresh token — JWT atual expira em 24h sem renovação automática | Backend | Médio |
| M5 | `isAuthenticated()` no Angular só verifica presença do token no `localStorage`, não a expiração | Frontend | Pequeno |
| M10 | Substituir `ddl-auto: update` por Flyway ou Liquibase para controle seguro de migrações de schema | Backend | Médio |

### Prioridade 2 — Melhorias de UX (próximas iterações)

| # | Tarefa | Área | Esforço |
|---|--------|------|---------|
| B3 | Busca por categoria e barra de pesquisa na home atualmente apenas fazem `console.log` sem filtrar nada | Frontend | Médio |
| B4 | Paginação visível em todas as listagens do frontend (backend já pagina, frontend não exibe controles) | Frontend | Médio |
| B5 | Erros de HTTP nos componentes legados usam `console.error` sem nenhum feedback visual ao usuário | Frontend | Pequeno |
| B1 | Sem rate limiting em `POST /api/auth/login` — vulnerável a força bruta | Backend | Pequeno |

### Prioridade 3 — Evolução do produto (maior esforço)

| Tarefa | Área | Esforço |
|--------|------|---------|
| Migrar templates HTML inline dos componentes legados para arquivos `.html` separados | Frontend | Médio |
| Sistema de notificações em tempo real (polling ou WebSocket) | Full-stack | Grande |
| Painel administrativo para gerenciar usuários, categorias e denúncias | Full-stack | Grande |
| Busca geolocalizada de prestadores por raio de distância | Full-stack | Grande |

---

## Autor

Felipe Andrade Lima — [GitHub](https://github.com/felipealima1984)
