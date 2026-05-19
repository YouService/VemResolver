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

> **Importante — rebuild do backend:** o frontend é servido com volume montado e hot-reload (`ng serve --poll 1000`). O backend é um JAR compilado — qualquer alteração no código Java exige rebuild explícito do container:
> ```bash
> docker-compose up --build -d backend
> ```
> Sem o rebuild, o container continuará executando o JAR antigo e endpoints novos retornarão 404.

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

### Seed de dados volumosos

Para popular o banco com dados realistas (10 clientes, 10 prestadores, 15 fluxos completos com chat e avaliações variadas), execute o script Python após subir o ambiente:

```bash
pip install requests   # apenas na primeira vez
python seed_dados.py
```

O script realiza o fluxo completo — solicitação → proposta → chat → aceitar → concluir → avaliação mútua — e exibe tudo no painel admin. Leva aproximadamente 3 minutos (inclui pausas obrigatórias pelo rate limit de login).

> **Nota:** execute apenas com o ambiente rodando (`docker-compose up`). Ao re-executar, limpe os dados seed primeiro: `docker exec marketplace-postgres psql -U postgres -d marketplace_db -c "DELETE FROM usuarios WHERE email LIKE '%@seed.com';"` (com as dependências na ordem correta).

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

### Testes e qualidade

- ✅ **117 testes unitários backend** passando — `PrestadorService`, `FotoPrestadorService`, `PropostaService`, `AvaliacaoService`, `NotificacaoService` (`fix/cobertura-de-testes`)
- ✅ **39 testes de segurança backend** passando — `PrestadorSegurancaTest`, `ChatSegurancaTest`, `AdminSegurancaTest`; 9 novos testes para os endpoints admin de online e solicitações; gap de segurança corrigido: `GET /api/prestadores/me/**` agora exige `ROLE_PRESTADOR`
- ✅ **83/84 testes frontend** passando — `ChatComponent`, `OnboardingChecklist`, `PerfilPublico`, `PrestadorDashboard`, `AdminDashboardComponent` (13 novos)
- ✅ **P4.5 E-mail transacional via Brevo** — 8 testes `BrevoEmailServiceTest` + 11 testes `AdminEmailSegurancaTest`
- ✅ **P4.6 Badge "Verificado"** — 2 testes em `PrestadorServiceTest`; totais antes do P4.7: 150 backend, 90/91 frontend
- ✅ **P4.7 Avaliação multidimensional — Tarefa 5** — Exibição das médias dimensionais no perfil público (card "Desempenho por Dimensão" com barras de progresso) e no painel do prestador (badges compactos no header); getters `temMediasDimensionais`/`dimensoesPerfil` em `PerfilPublicoComponent` e `dimensoesDashboard` em `PrestadorDashboardComponent`; `Prestador` e `PerfilPublico` recebem `mediaPontualidade`, `mediaQualidade`, `mediaComunicacao`; 9 novos testes Jasmine (4 em `PerfilPublicoComponent` + 4 em `PrestadorDashboardComponent` mais 1 de edge-case); **totais: 158 testes backend**, 116/117 frontend
- ✅ **P4.7 Avaliação multidimensional — Tarefa 6 (COMPLETO)** — Testes de integração de controller com MockMvc: `AvaliacaoDimensionalControllerTest` (7 testes: POST com 3 dimensões → 201 + campos no body, POST sem dimensões → campos ausentes, `@Min`/`@Max` violados → 400, nota geral ausente → 400, sem auth → 403, `solicitacaoId` ausente → 400) + `PerfilPublicoDimensionalControllerTest` (5 testes: GET com médias dimensionais, sem médias, apenas pontualidade, sem auth → 200, prestador inexistente → 404); **totais: 170 testes backend**, 116/117 frontend · P4.7 totalmente entregue

### Prioridade 4 — Competitividade (em andamento)

- ✅ **P4.1 Chat interno** entre cliente e prestador — um chat privado por proposta (pré e pós aceite); polling 3s; somente leitura após encerramento ou proposta recusada; botão "💬 Chat" integrado nos dashboards e na tela de propostas recebidas; 11 testes unitários `ChatServiceTest` (CT-01-01 a CT-01-11)
- ✅ **fix/chat-p4-1** — DataSeeder corrigido para criar perfil de prestador, solicitação ABERTA e proposta ENVIADA de teste; chat disponível imediatamente após `docker-compose up` sem precisar de reset de banco
- ✅ **P4.2 Portfólio de fotos** — prestador adiciona até 10 fotos (JPEG/PNG/WEBP, máx 5 MB cada) em "Meu Perfil"; grid com remoção por hover; armazenamento em volume Docker persistente; `GET /api/prestadores/{id}/fotos` público; completude do perfil passa a incluir fotos como 5º critério
- ✅ **Painel do Prestador redesenhado** — oportunidades aparecem primeiro (com filtro por cidade/estado); contratos em andamento, propostas aguardando e serviços concluídos abaixo; visual e estrutura consistentes com o painel do cliente; tratamento de erro e filtragem reativa
- ✅ **P4.5 E-mail transacional via Brevo** — Admin configura a API key do Brevo, e-mail remetente e toggle ativo/inativo na aba "✉️ E-mail" do painel admin; botão "Enviar e-mail de teste"; API key mascarada na exibição (sem exposição); `BrevoEmailService` usa Java `HttpClient` (sem SMTP/Spring Mail); disparos automáticos: proposta recebida → cliente, proposta aceita → prestador, contrato concluído → prestador; `V11__add_configuracoes_email.sql`; endpoints: `GET/PUT /api/admin/email/config`, `POST /api/admin/email/testar` (todos `ROLE_ADMIN`)
- ✅ **P4.6 Badge "Verificado"** — badge automático para prestadores com ≥5 contratos concluídos e média ≥4,0; coluna `verificado BOOLEAN` persistida em `prestadores` (`V12__add_verificado_prestador.sql`); recalculado automaticamente a cada nova avaliação de cliente e a cada conclusão de serviço; exibido no perfil público, no painel do prestador e nos cards da busca por geolocalização; `PrestadorDTO.verificado` exposto na API; 2 novos testes unitários em `PrestadorServiceTest`
- ✅ **P4.4 Perfil público compartilhável** — página pública em `/prestador/:id` acessível sem login; exibe avatar, nome, badge "Verificado" (≥5 contratos + média ≥4,0), categorias, portfólio em grid com ampliação, últimas 5 avaliações; botão "Solicitar Serviço" redireciona para `/register` se visitante ou `/cliente/nova-solicitacao` se cliente logado; botão "Copiar link" com Clipboard API; botão "Ver perfil público" e "Copiar link do perfil" no painel do prestador; `GET /api/prestadores/{id}/publico` (sem dados sensíveis); 404 amigável para perfil não encontrado ou inativo
- ✅ **P4.3 Onboarding guiado** — checklist de completude do perfil com 7 critérios ponderados (foto de perfil 15%, descrição 20%, categorias 15%, telefone 10%, cidade+estado 10%, portfólio 20%, lat/lng 10%); barra de progresso com cor dinâmica (vermelho <50%, amarelo 50–79%, verde ≥80%); collapse automático ao 100% com mensagem de parabéns; `OnboardingChecklistComponent` exibido no topo do painel do prestador; avatar upload via `POST /api/prestadores/me/foto-perfil`; campo de telefone editável na tela de perfil; `percentual_completude` persistido em `prestadores` e recalculado a cada save ou upload de foto; endpoint `GET /api/prestadores/me/completude` com `CompletudePrestadorDTO`; `V9__add_foto_perfil_e_completude.sql`

### Prioridade 4 — P4.7 Avaliação multidimensional (em progresso)

- ✅ **Tarefa 1 — Migração e modelo de dados** (`feature/p4.7-avaliacao-multidimensional`): `V13` adiciona `nota_pontualidade`, `nota_qualidade`, `nota_comunicacao` (`INTEGER NULL`) em `avaliacoes`; `Avaliacao.java` recebe os 3 campos nullable; 3 testes unitários
- ✅ **Tarefa 2 — Médias por dimensão no prestador** (`feature/p4.7-t2-medias-dimensionais-prestador`): `V14` adiciona `media_pontualidade`, `media_qualidade`, `media_comunicacao` (`DOUBLE NULL`) em `prestadores`; `Prestador.java` e `PrestadorDTO.java` recebem os 3 campos; `AvaliacaoService.atualizarMediaPrestador()` recalcula as 3 médias a cada nova avaliação; 3 novos testes em `AvaliacaoServiceTest`
- ✅ **Tarefa 3 — DTO e endpoint** (`feature/p4.7-t3-dto-endpoint`): `AvaliacaoDTO` recebe `notaPontualidade`, `notaQualidade`, `notaComunicacao` (Integer, opcionais, `@Min(1) @Max(5)`); `AvaliacaoService.criar()` persiste as dimensões do DTO; `toDTO()` as mapeia no retorno; `PerfilPublicoDTO` expõe as 3 médias dimensionais do prestador; 3 testes em `AvaliacaoServiceTest` + 2 em `PrestadorServiceTest`; **totais: 161 testes backend**, 90/91 frontend
- ✅ **Tarefa 4 — Formulário frontend** (`feature/p4.7-t4-form-dimensional`): `ClienteAvaliacaoComponent` atualizado com 3 grupos de estrelas opcionais (pontualidade, qualidade do serviço, comunicação); `DimensaoRating` interface; payload enviado inclui apenas dimensões com nota > 0; `Avaliacao` interface do modelo frontend recebe os 3 campos opcionais; 13 novos testes Jasmine; **totais: 161 testes backend**, 103/104 frontend
- ✅ **Tarefa 5 — Exibição frontend** (`feature/p4.7-t5-exibicao-dimensional`): `PerfilPublicoComponent` exibe card "Desempenho por Dimensão" com barras de progresso proporcionais (escala 0–5 → 0–100%); `PrestadorDashboardComponent` exibe badges compactos no header quando há avaliações dimensionais; `Prestador` e `PerfilPublico` recebem os 3 campos opcionais; getters `temMediasDimensionais`, `dimensoesPerfil`, `dimensoesDashboard`; 9 novos testes; **totais: 158 testes backend**, 116/117 frontend
- ✅ **Tarefa 6 — Testes de integração** (`feature/p4.7-t6-testes-integracao`): `AvaliacaoDimensionalControllerTest` (WebMvcTest, 7 testes: validações `@Min`/`@Max` nas dimensões, payload com e sem dimensões, segurança 403, 400 validation); `PerfilPublicoDimensionalControllerTest` (WebMvcTest, 5 testes: resposta JSON com médias dimensionais, ausência de campos nulos, endpoint público sem auth, 404 para inexistente); **totais: 170 testes backend**, 116/117 frontend

### Prioridade 3 — Evolução do produto (concluída)

- ✅ Migração de todos os templates HTML inline para arquivos `.html` separados — 19 componentes migrados
- ✅ Sistema de notificações em tempo real — polling a cada 30 s; sino com badge no navbar; painel dropdown com "Marcar todas como lidas"; triggers automáticos em proposta enviada, aceita, serviço concluído e avaliação recebida
- ✅ Painel administrativo — estatísticas globais (usuários, solicitações, prestadores, avaliações, **online agora**), tabela de usuários paginada com ativação/desativação inline; aba **"Online agora"** com grid de usuários ativos nos últimos 5 minutos (dot animado); aba **"Solicitações"** com filtro por status e alteração inline de status; `OnlineTrackingFilter` registra `ultimo_acesso` throttled a cada 2 min; `GET /api/admin/usuarios/online`, `GET /api/admin/solicitacoes`, `PATCH /api/admin/solicitacoes/{id}/status` — todos restritos a `ROLE_ADMIN`
- ✅ Busca geolocalizada de prestadores por raio — `navigator.geolocation` no frontend; Haversine no backend (`GET /api/prestadores/proximo?lat&lng&raioKm`); prestador salva coordenadas via botão "Usar minha localização atual" no perfil; `V3__add_notificacoes_e_geolocalizacao.sql` adiciona `latitude`/`longitude` e tabela `notificacoes`

---

## O que fazer a seguir

### Prioridade 4 — Competitividade com GetNinjas e Cronoshare

Análise competitiva realizada em 2026-05-15 com base nos sites [GetNinjas](https://www.getninjas.com.br/) e [Cronoshare](https://www.cronoshare.com.br/). Os itens abaixo representam os gaps identificados em relação aos concorrentes, ordenados por impacto × esforço.

| # | Funcionalidade | Justificativa | Impacto | Esforço |
|---|---|---|---|---|
| P4.1 | **Chat interno** — um chat por proposta, disponível antes e após o aceite | Permite negociação antes do aceite; mantém histórico dentro da plataforma; abre caminho para monetização por contato | Alto | Médio | ✅ Entregue |
| P4.2 | **Portfólio de fotos** no perfil do prestador (até 10 imagens de trabalhos realizados) | Fotos são o principal elemento de conversão em serviços visuais (pintura, reforma, cozinha) | Alto | Baixo | ✅ Entregue |
| P4.3 | **Onboarding guiado** com barra de progresso e checklist do perfil | Prestadores sem guia visual ficam com perfis fracos; reduz taxa de prestadores cadastrados mas inativos | Médio-Alto | Baixo | ✅ Entregue |
| P4.4 | **Perfil público compartilhável** em `/prestador/{id}` com CTA para solicitar serviço | Prestador não tem URL pública para divulgar fora da plataforma; gera tráfego orgânico de volta | Médio-Alto | Baixo | ✅ Entregue |
| P4.5 | **Notificações por e-mail** transacionais (nova proposta recebida, proposta aceita, nova solicitação compatível) | Polling de 30 s só funciona com a aba aberta; e-mail garante alcance offline | Alto | Baixo-Médio | ✅ Entregue |
| P4.6 | **Badge "Verificado"** automático para prestadores com 5+ contratos concluídos e média ≥ 4,0 | Nenhum diferencial visual entre prestadores hoje; badge cria gamificação de reputação | Médio | Baixo | ✅ Entregue |
| P4.7 | **Avaliação multidimensional** — pontualidade, qualidade do serviço e comunicação (além da nota geral) | Nota única 1–5 é pouco granular; dimensões separadas ajudam o cliente a escolher e o prestador a melhorar | Médio | Médio | ✅ Entregue |

---

## Próximos passos

Todas as prioridades P1–P4 estão concluídas. O backlog completo de funcionalidades futuras está documentado em [`docs/BACKLOG.md`](docs/BACKLOG.md), organizado em fases:

| Fase | Foco | Prioridade |
|---|---|---|
| Fase 1 | Deploy + CI/CD + Correção de vulnerabilidades (P5) | 🔴 Crítico |
| Fase 2 | Monetização: créditos pré-pagos + gateway de pagamento (P6) | 🟠 Alta |
| Fase 3 | Crescimento: busca full-text + categorias + mobile (P7, P11) | 🟠 Alta |
| Fase 4 | Confiança: verificação de identidade + denúncias (P8, P10) | 🟡 Média |
| Fase 5 | Segurança avançada: refresh token rotation + auditoria (P12) | 🟡 Média |
| Fase 6 | Engajamento: push notifications + chat com imagens (P9) | 🟢 Normal |

---

## Autor

Felipe Andrade Lima — [GitHub](https://github.com/felipealima1984)
