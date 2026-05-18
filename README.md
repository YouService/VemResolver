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
| [`docs/01-requisitos-e-visao.txt`](docs/01-requisitos-e-visao.txt) | Documento oficial de requisitos — RF-01 a RF-14, RNF-01 a RNF-08, histórias de usuário |
| [`docs/02-arquitetura-e-banco.txt`](docs/02-arquitetura-e-banco.txt) | Arquitetura de software — camadas, Docker, segurança, modelo de dados completo |
| [`docs/03-implementacao-e-apis.txt`](docs/03-implementacao-e-apis.txt) | Guia de implementação — contratos de API, DTOs, serviços Angular, boas práticas |
| [`docs/04-testes-e-qualidade.txt`](docs/04-testes-e-qualidade.txt) | Plano de testes — inventário atual, gaps críticos, esqueletos de testes prioritários |
| [`docs/CASOS-DE-USO-P4.md`](docs/CASOS-DE-USO-P4.md) | Casos de uso detalhados da Prioridade 4 — UC-01 a UC-07 com fluxos, regras e critérios de aceite |
| [`docs/CASOS-DE-TESTE-P4.md`](docs/CASOS-DE-TESTE-P4.md) | Casos de teste da Prioridade 4 — 82 CTs cobrindo fluxo principal, negativo, borda, segurança e auditoria para UC-01 a UC-07 |

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
- ✅ Campo `dataCriacao` na entidade `Avaliacao` e migration `V7` — coluna adicionada à tabela `avaliacoes` (tabela havia sido criada pelo Hibernate DDL antes do Flyway, sem a coluna; `V7__add_data_criacao_avaliacoes.sql` corrige o schema no banco existente)
- ✅ `@Transactional(readOnly = true)` em todos os métodos de leitura com múltiplas chamadas a repositórios — corrige 500 em `SolicitacaoServicoService`, `PropostaService` e `AvaliacaoService`

### Segurança e infraestrutura

- ✅ CORS via variável de ambiente `${FRONTEND_URL}` — sem hardcode
- ✅ URLs da API Angular centralizadas em `environment.ts` (`/api` em prod, `localhost:8080` em dev)
- ✅ Interceptor Angular de 401 — tenta refresh automático; só redireciona para `/login` se refresh falhar
- ✅ Swagger desabilitado em produção (`application-prod.yml`)
- ✅ `DataSeeder` restrito ao perfil `!prod` — sem usuários de teste em produção
- ✅ Ownership em aceitar/recusar propostas e em alterar status de solicitação
- ✅ Endpoints de categoria (`POST/PUT/DELETE`) restritos a `ROLE_ADMIN` — via `SecurityFilterChain` + `@PreAuthorize`
- ✅ Criação de perfil de prestador restrita a `ROLE_PRESTADOR`
- ✅ `UserController` legado removido — substituído por `PerfilController` com DTOs seguros (`UpdatePerfilDTO`)
- ✅ Rodapé padronizado em todas as páginas públicas (logo + links + copyright)
- ✅ `PropostaController` desacoplado do repositório — resolução de prestador movida para `PropostaService`
- ✅ Refresh token com rotação — sessão renova automaticamente; `POST /api/auth/refresh`; token de 7 dias
- ✅ `isAuthenticated()` decodifica o JWT para checar expiração real no client
- ✅ Flyway substitui `ddl-auto: update` — schema versionado em `db/migration/`; `baseline-on-migrate` para DBs existentes
- ✅ `V4` migration — `ADMIN` adicionado ao `CHECK CONSTRAINT` de `usuarios.tipo`; `DataSeeder` corrigido para verificar cada usuário de teste individualmente por e-mail (antes usava `count() == 0` e nunca criava o admin em bancos com dados)

### Melhorias de UX (Prioridade 2 — concluída)

- ✅ B3: Busca e categorias na home navegam corretamente — `selecionarCategoria()` e `buscarServico()` redirecionam para cadastro/dashboard conforme o papel do usuário; `ClienteNovaSolicitacaoComponent` pré-seleciona categoria via query param `?categoriaId`
- ✅ B4: Controles de paginação visíveis nos dashboards de cliente e prestador — botões anterior/próxima + indicador "Página X de Y"; backend já paginava, agora o frontend exibe e navega entre páginas
- ✅ B5: Todos os `console.error` sem feedback visual foram substituídos por mensagens de erro exibidas ao usuário — banners de erro nos dashboards, erros de CEP no perfil, erro de carregamento de categorias na home
- ✅ B1: Rate limiting em `POST /api/auth/login` — `LoginRateLimitFilter` bloqueia IPs com mais de 10 tentativas por minuto com HTTP 429

### Prioridade 4 — Competitividade (em andamento)

- ✅ **P4.1 Chat interno** entre cliente e prestador — um chat privado por proposta (pré e pós aceite); polling 3s; somente leitura após encerramento ou proposta recusada; botão "💬 Chat" integrado nos dashboards e na tela de propostas recebidas; 11 testes unitários `ChatServiceTest` (CT-01-01 a CT-01-11)
- ✅ **fix/chat-p4-1** — DataSeeder corrigido para criar perfil de prestador, solicitação ABERTA e proposta ENVIADA de teste; chat disponível imediatamente após `docker-compose up` sem precisar de reset de banco
- ✅ **P4.2 Portfólio de fotos** — prestador adiciona até 10 fotos (JPEG/PNG/WEBP, máx 5 MB cada) em "Meu Perfil"; grid com remoção por hover; armazenamento em volume Docker persistente; `GET /api/prestadores/{id}/fotos` público; completude do perfil passa a incluir fotos como 5º critério

### Prioridade 3 — Evolução do produto (concluída)

- ✅ Migração de todos os templates HTML inline para arquivos `.html` separados — 19 componentes migrados
- ✅ Sistema de notificações em tempo real — polling a cada 30 s; sino com badge no navbar; painel dropdown com "Marcar todas como lidas"; triggers automáticos em proposta enviada, aceita, serviço concluído e avaliação recebida
- ✅ Painel administrativo — estatísticas globais (usuários, solicitações, prestadores, avaliações), tabela de usuários paginada com ativação/desativação inline; endpoint `GET /api/admin/stats`, restrito a `ROLE_ADMIN`
- ✅ Busca geolocalizada de prestadores por raio — `navigator.geolocation` no frontend; Haversine no backend (`GET /api/prestadores/proximo?lat&lng&raioKm`); prestador salva coordenadas via botão "Usar minha localização atual" no perfil; `V3__add_notificacoes_e_geolocalizacao.sql` adiciona `latitude`/`longitude` e tabela `notificacoes`

---

## O que fazer a seguir

### Prioridade 4 — Competitividade com GetNinjas e Cronoshare

Análise competitiva realizada em 2026-05-15 com base nos sites [GetNinjas](https://www.getninjas.com.br/) e [Cronoshare](https://www.cronoshare.com.br/). Os itens abaixo representam os gaps identificados em relação aos concorrentes, ordenados por impacto × esforço.

| # | Funcionalidade | Justificativa | Impacto | Esforço |
|---|---|---|---|---|
| P4.1 | **Chat interno** — um chat por proposta, disponível antes e após o aceite | Permite negociação antes do aceite; mantém histórico dentro da plataforma; abre caminho para monetização por contato | Alto | Médio | ✅ Entregue |
| P4.2 | **Portfólio de fotos** no perfil do prestador (até 10 imagens de trabalhos realizados) | Fotos são o principal elemento de conversão em serviços visuais (pintura, reforma, cozinha) | Alto | Baixo | ✅ Entregue |
| P4.3 | **Onboarding guiado** com barra de progresso e checklist do perfil | Prestadores sem guia visual ficam com perfis fracos; reduz taxa de prestadores cadastrados mas inativos | Médio-Alto | Baixo |
| P4.4 | **Perfil público compartilhável** em `/prestador/{id}` com CTA para solicitar serviço | Prestador não tem URL pública para divulgar fora da plataforma; gera tráfego orgânico de volta | Médio-Alto | Baixo |
| P4.5 | **Notificações por e-mail** transacionais (nova proposta recebida, proposta aceita, nova solicitação compatível) | Polling de 30 s só funciona com a aba aberta; e-mail garante alcance offline | Alto | Baixo-Médio |
| P4.6 | **Badge "Verificado"** automático para prestadores com 5+ contratos concluídos e média ≥ 4,0 | Nenhum diferencial visual entre prestadores hoje; badge cria gamificação de reputação | Médio | Baixo |
| P4.7 | **Avaliação multidimensional** — pontualidade, qualidade do serviço e comunicação (além da nota geral) | Nota única 1–5 é pouco granular; dimensões separadas ajudam o cliente a escolher e o prestador a melhorar | Médio | Médio |

---

## Autor

Felipe Andrade Lima — [GitHub](https://github.com/felipealima1984)
