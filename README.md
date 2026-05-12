# VemResolver — Marketplace de Serviços

Plataforma web que conecta clientes a prestadores de serviços autônomos. O fluxo central é: cliente abre uma solicitação → prestadores enviam propostas → cliente aceita → serviço é executado → cliente avalia.

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

Criados automaticamente pelo `DataSeeder` na primeira inicialização:

| Papel | Email | Senha |
|-------|-------|-------|
| Cliente | `joao@cliente.com` | `123456` |
| Prestador | `maria@prestador.com` | `123456` |

---

## Documentação técnica

| Documento | Descrição |
|-----------|-----------|
| [`docs/DAS.md`](docs/DAS.md) | Arquitetura de Software — camadas, pacotes, modelo de dados |
| [`docs/REGRAS-DE-NEGOCIO.md`](docs/REGRAS-DE-NEGOCIO.md) | Fluxo completo do marketplace, estados, autorizações |
| [`docs/API.md`](docs/API.md) | Contratos OpenAPI dos endpoints principais |
| [`docs/LEVANTAMENTO.md`](docs/LEVANTAMENTO.md) | Levantamento técnico — estado atual, débitos e plano de ação |

---

## Histórico de Desenvolvimento

### 2026-05-11 — Felipe Lima

**Infraestrutura / ambiente**
- Corrigidos os nomes das variáveis de ambiente no `docker-compose.yml` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) que estavam errados e impediam o backend de subir
- Adicionado `--host 0.0.0.0 --poll 1000` ao `ng serve` para o frontend funcionar dentro do Docker no Windows

**Bug corrigido**
- `fix/perfil-prestador-banner` — Banner "complete seu perfil" no painel do prestador não desaparecia após salvar o perfil. Causa: `carregarPerfil()` chamava `buscarPorUsuario(userId)` mapeado para `/api/prestadores/usuario/:id`, endpoint que não existe. Corrigido para `buscarMeuPerfil()` → `GET /api/prestadores/me`

**Funcionalidades entregues**
- `feature/envio-proposta-prestador` — Tela completa de envio de proposta para o prestador em `/prestador/solicitacao/:id`:
  - Exibe todos os detalhes da solicitação (descrição, cliente, local, categoria)
  - Formulário com valor (R$) e mensagem, com validações
  - Estado de sucesso após envio e tratamento de erro da API (ex: proposta duplicada)
  - Botão "Ver Detalhes e Enviar Proposta" no dashboard do prestador agora navega para esta tela

- `feature/propostas-recebidas-cliente` — Tela de propostas recebidas para o cliente em `/cliente/solicitacao/:id/propostas`:
  - Lista todas as propostas: nome do prestador, valor em destaque, mensagem, data, status
  - Aceitar proposta: confirmação inline antes de executar (informa que a solicitação vai para Em Andamento)
  - Recusar proposta: executa direto
  - Ações visíveis apenas para propostas `ENVIADA` com solicitação `ABERTA`
  - Banner de sucesso pós-aceite
  - Botão "Ver Propostas" adicionado nos cards e no modal de detalhes do dashboard do cliente

**Documentação gerada**
- `docs/DAS.md`, `docs/REGRAS-DE-NEGOCIO.md`, `docs/API.md`, `docs/LEVANTAMENTO.md`

### 2026-05-12 — Felipe Lima

**Funcionalidade entregue**
- `feature/marcar-servico-concluido` — Ação de marcar serviço como Concluído:
  - Endpoint dedicado `PATCH /api/solicitacoes/{id}/concluir` com validação: apenas o cliente dono pode chamar, e somente quando a solicitação está em `EM_ANDAMENTO`
  - Botão "Marcar como Concluído" visível nos cards do dashboard e no modal de detalhes, exclusivamente para solicitações `EM_ANDAMENTO`
  - Modal de confirmação antes de executar a ação
  - Toast de sucesso com auto-dismiss em 5s: "Serviço marcado como concluído! Você já pode avaliar o prestador."
  - Testes: 5 unit tests Mockito (`SolicitacaoServicoServiceTest`), spec Angular com 6 casos (`ClienteDashboardComponent.spec`), e fluxo de integração estendido com envio de proposta + aceite + conclusão

**Infraestrutura**
- `docker-compose.yml` corrigido: adicionadas variáveis `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` e `FRONTEND_URL` que estavam faltando e impediam o rebuild da imagem de subir

**Bug corrigido**
- `fix/cors-patch-method` — `PATCH` ausente em `setAllowedMethods` no `SecurityConfig.java` causava 403 no preflight CORS de todas as ações de aceitar/recusar proposta e marcar serviço como concluído

**Funcionalidade entregue (2)**
- `feature/prestador-contratos-andamento` — Painel do prestador agora reflete propostas aceitas:
  - `PropostaDTO` enriquecido com dados da solicitação (descrição, status, cliente, localização, categoria)
  - Seção "Contratos em Andamento" com cards destacados para propostas aceitas pelo cliente
  - Seção "Propostas Aguardando Resposta" para propostas ainda pendentes
  - Stats reais: oportunidades abertas, aguardando resposta, em andamento, concluídos (antes todos hardcoded em 0)

**Funcionalidade entregue (3)**
- `feature/tela-avaliacao` — Tela de avaliação do prestador em `/cliente/solicitacao/:id/avaliacao`:
  - Estrelas interativas 1–5 com hover, texto descritivo da nota e validação
  - Exibe nome do prestador (buscado da proposta aceita) e dados da solicitação
  - Campo de comentário opcional com contador de caracteres (máx. 500)
  - Estados: carregando, formulário, enviando, sucesso, erro (incluindo "já avaliado")
  - Botão "Avaliar" (amarelo) nos cards do dashboard para solicitações concluídas
  - Toast de conclusão atualizado com botão "Avaliar Agora ★" e redirect direto

### 2026-05-12 — Felipe Lima (continuação 3)

**Fixes de UI entregues**
- Correção de overflow horizontal no painel do prestador de serviços:
  - `min-w-0` em todos os containers `flex-1` dos cards (Contratos, Propostas, Concluídos, Oportunidades)
  - `flex-shrink-0` em valores monetários e datas; `break-words` em descrições; `truncate` em nomes e endereços
  - `overflow-hidden` nos cards de contratos e oportunidades como barreira de segurança
- Botão "Ver Propostas" no painel do cliente ocultado para serviços `CONCLUIDA` e `CANCELADA` (idem no modal de detalhes)
- Correção de overflow horizontal nos cards do painel do cliente (`min-w-0`, `break-words`, `flex-wrap`)
- Bug #4 corrigido: formulário de perfil do prestador agora pré-preenche os campos corretamente
  - `buscarPorUsuario(userId)` → `buscarMeuPerfil()` (`GET /api/prestadores/me` via JWT)
- Aba "Minhas Avaliações" do perfil do cliente simplificada: exibe apenas nota média e estrelas agregadas (remove lista de avaliações individuais; reutiliza `User.avaliacaoMedia` e `totalAvaliacoes` já carregados por `buscarPerfil()`)

---

### 2026-05-12 — Felipe Lima (continuação 2)

**Fix entregue**
- `fix/avaliacao-regras-visibilidade` — Correção da regra de unicidade de avaliação por serviço e visibilidade de estrelas nos dashboards:
  - Backend: `SolicitacaoServicoDTO` enriquecido com `foiAvaliadoPeloCliente` e `foiAvaliadoPeloPrestador` (populados por `existsBySolicitacaoIdAndTipo`)
  - Backend: `PropostaDTO` enriquecido com `foiAvaliadoPeloPrestador`
  - Frontend: Dashboards do cliente e do prestador exibem badge "Avaliado ✓" quando o serviço já foi avaliado, evitando confusão com a regra de unicidade por `solicitacaoId`
  - Frontend: Cabeçalho do dashboard do prestador mostra a média de estrelas recebidas (`prestador.avaliacaoMedia`)
  - Frontend: Cabeçalho do dashboard do cliente mostra a média de estrelas recebidas via `buscarPerfil()` → `User.avaliacaoMedia`
  - Frontend: Correção de typo `dataExpedicao` → `dataExpedicaoRg` em `user-profile.component.ts`

---

### 2026-05-12 — Felipe Lima (continuação)

**Funcionalidade entregue**
- `feature/avaliacao-bidirecional` — Avaliação de mão dupla: prestador avalia cliente, notas visíveis nos perfis:
  - Backend: enum `TipoAvaliacao` discrimina direção da avaliação; campo `tipo` adicionado em `Avaliacao`
  - Backend: `User.avaliacaoMedia` e `User.totalAvaliacoes` armazenam a reputação do cliente (atualizada automaticamente)
  - Backend: `POST /api/avaliacoes/para-cliente` — prestador avalia cliente após serviço concluído; valida que a proposta foi ACEITA
  - Backend: `GET /api/avaliacoes/cliente/{clienteId}` — lista avaliações recebidas pelo cliente
  - Frontend: `PrestadorAvaliacaoComponent` em `/prestador/solicitacao/:id/avaliacao` com estrelas interativas e estados completos
  - Frontend: Seção "Serviços Concluídos" no painel do prestador com botão "Avaliar Cliente ★" por contrato
  - Frontend: Aba "Minhas Avaliações" no perfil do cliente mostrando nota média e histórico de avaliações recebidas
  - Frontend: Card de reputação no perfil do prestador exibindo `avaliacaoMedia` com estrelas em destaque

---

## O que fazer a seguir

Ordem sugerida por impacto no fluxo central do marketplace:

### 1. ~~Marcar serviço como CONCLUÍDO~~ ✅ *Entregue em 2026-05-12*
Endpoint dedicado `PATCH /api/solicitacoes/:id/concluir` com validação de propriedade e transição de status.
Botão "Marcar como Concluído" no dashboard do cliente para solicitações `EM_ANDAMENTO`, com modal de confirmação e toast de sucesso.
Branch: `feature/marcar-servico-concluido`.

### 2. ~~Tela de avaliação do prestador~~ ✅ *Entregue em 2026-05-12*
`ClienteAvaliacaoComponent` em `/cliente/solicitacao/:id/avaliacao` com estrelas interativas, comentário opcional e estados de loading/sucesso/erro. Botão "Avaliar" nos cards concluídos e link "Avaliar Agora" no toast de conclusão.
Branch: `feature/tela-avaliacao`.

### 3. ~~Avaliação bidirecional~~ ✅ *Entregue em 2026-05-12*
Prestador avalia cliente, nota média exibida no perfil de ambos. `POST /api/avaliacoes/para-cliente`, `GET /api/avaliacoes/cliente/{id}`.
Branch: `feature/avaliacao-bidirecional`.

### 3.1. ~~Fix: unicidade de avaliação por serviço e visibilidade de estrelas~~ ✅ *Entregue em 2026-05-12*
DTOs enriquecidos com flags `foiAvaliadoPeloCliente`/`foiAvaliadoPeloPrestador`; dashboards exibem "Avaliado ✓" e médias de estrelas no cabeçalho.
Branch: `fix/avaliacao-regras-visibilidade`.

### 4. ~~Bug: formulário de edição do perfil do prestador nunca pré-preenche~~ ✅ *Corrigido em 2026-05-12*
`buscarPorUsuario(userId)` substituído por `buscarMeuPerfil()` → `GET /api/prestadores/me`.

### 5. Bug: cadastro de novos usuários retorna 400
`RegisterDTO` no backend tem `@NotBlank` em CPF, RG e data de expedição, mas o frontend não envia esses campos.
Decidir: remover as anotações `@NotBlank` do backend (mais simples) ou adicionar os campos no formulário de cadastro.

### 6. URLs da API hardcoded nos services Angular
Todos os services usam `http://localhost:8080` diretamente. Centralizar em `src/environments/environment.ts` antes de qualquer deploy em outro ambiente.

### 7. CORS hardcoded
`SecurityConfig.java:63` — CORS fixo em `localhost:4200`. Parametrizar via variável de ambiente antes de qualquer deploy.

### 8. Interceptor de 401
Sem interceptor de resposta para redirecionar ao login após token expirado. O usuário fica preso com telas em branco.

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

## Autor

Felipe Andrade Lima — [GitHub](https://github.com/felipealima1984)
