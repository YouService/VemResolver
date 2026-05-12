# Levantamento Técnico — VemResolver (YouService Marketplace)

> **Data:** 2026-05-11  
> **Analista:** Claude Sonnet 4.6  
> **Branch analisada:** main

---

## 1. VISÃO GERAL

### Nome e propósito
**YouService Marketplace** — plataforma que conecta clientes que precisam de serviços domésticos/profissionais com prestadores autônomos. O modelo de negócio é o mesmo do GetNinjas: cliente abre uma solicitação, prestadores enviam propostas com valor, cliente aceita uma proposta, serviço é executado, cliente avalia o prestador.

### Stack tecnológico real

| Camada | Tecnologia | Versão exata |
|--------|-----------|--------------|
| Backend framework | Spring Boot | **3.2.1** |
| Linguagem backend | Java | **21** |
| ORM | Spring Data JPA / Hibernate | via Spring Boot 3.2.1 |
| Segurança | Spring Security | via Spring Boot 3.2.1 |
| JWT | JJWT (io.jsonwebtoken) | **0.12.3** |
| Banco de dados | PostgreSQL | **16** |
| Documentação API | Springdoc OpenAPI (webmvc-ui) | **2.3.0** |
| Utilitários backend | Lombok | via Spring Boot BOM |
| Variáveis de ambiente | dotenv-java (cdimascio) | **3.0.0** |
| Build backend | Maven | 3.9.x |
| Frontend framework | Angular | **19.2** |
| Linguagem frontend | TypeScript | **~5.7.2** |
| Estilização | Tailwind CSS | **^3.4.17** |
| Reatividade | RxJS | **~7.8.0** |
| UI Components | Angular Material | **^19.2.19** |
| Build frontend | Angular CLI | **^19.2.19** |
| Bundler frontend | esbuild | via Angular CLI |
| Infraestrutura | Docker + Docker Compose | 29.x |

---

## 2. ARQUITETURA ATUAL

### Estrutura real de pastas — Backend

```
backend/src/main/java/com/marketplace/servicos/
├── config/
│   ├── DataSeeder.java          (seed de categorias e usuários de teste)
│   ├── DotenvConfig.java        (carrega .env)
│   ├── EncodingFilter.java      (filtro UTF-8)
│   ├── OpenApiConfig.java       (configuração Swagger)
│   ├── SecurityConfig.java      (Spring Security + CORS)
│   └── WebConfig.java
├── controller/
│   ├── AuthController.java
│   ├── AvaliacaoController.java
│   ├── CategoriaController.java
│   ├── PrestadorController.java
│   ├── PropostaController.java
│   ├── SolicitacaoServicoController.java
│   └── UserController.java
├── dto/
│   ├── AuthResponseDTO.java
│   ├── AvaliacaoDTO.java
│   ├── CategoriaDTO.java
│   ├── LoginDTO.java
│   ├── PrestadorDTO.java
│   ├── PropostaDTO.java
│   ├── RegisterDTO.java
│   └── SolicitacaoServicoDTO.java
├── exception/
│   ├── BusinessException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Avaliacao.java
│   ├── Categoria.java
│   ├── Prestador.java
│   ├── Proposta.java
│   ├── SolicitacaoServico.java
│   ├── StatusProposta.java      (enum: ENVIADA, ACEITA, RECUSADA)
│   ├── StatusSolicitacao.java   (enum: ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA)
│   ├── TipoUsuario.java         (enum: CLIENTE, PRESTADOR)
│   └── User.java
├── repository/
│   ├── AvaliacaoRepository.java
│   ├── CategoriaRepository.java
│   ├── PrestadorRepository.java
│   ├── PropostaRepository.java
│   ├── SolicitacaoServicoRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
└── service/
    ├── AuthService.java
    ├── AvaliacaoService.java
    ├── CategoriaService.java
    ├── PrestadorService.java
    ├── PropostaService.java
    ├── SolicitacaoServicoService.java
    └── UserService.java
```

### Estrutura real de pastas — Frontend

```
frontend/marketplace-frontend/src/app/
├── components/
│   ├── cliente-dashboard.component.ts     (409 linhas, template inline)
│   ├── cliente-nova-solicitacao.component.ts
│   ├── home.component.ts                  (241 linhas, template inline)
│   ├── login.component.ts                 (usa Angular Material)
│   ├── navbar.component.ts
│   ├── prestador-dashboard.component.ts   (243 linhas, template inline)
│   ├── prestador-perfil.component.ts      (335 linhas, template inline)
│   ├── register.component.ts
│   └── user-profile.component.ts
├── guards/
│   └── auth.guard.ts                      (authGuard, clienteGuard, prestadorGuard)
├── interceptors/
│   └── auth.interceptor.ts
├── models/
│   └── models.ts
└── services/
    ├── auth.service.ts
    ├── avaliacao.service.ts
    ├── categoria.service.ts
    ├── prestador.service.ts
    ├── proposta.service.ts
    ├── solicitacao.service.ts
    └── user.service.ts
```

### Camadas existentes e ausentes

| Camada | Backend | Frontend |
|--------|---------|----------|
| Controller / Component | ✅ | ✅ |
| Service | ✅ | ✅ (services Angular) |
| Repository | ✅ | — |
| DTO | ✅ (parcial — `UserController` retorna entidade direta) | ✅ (models.ts) |
| Domain Model | ✅ | — |
| Módulos por domínio | ❌ (tudo em pacotes planos por tipo) | ❌ (tudo em `components/`) |
| Validação de negócio | ✅ (parcial, em Service) | ✅ (parcial, em formulários) |
| Testes | ❌ (zero testes) | ❌ (1 teste trivial) |

### Violações de boas práticas com exemplos reais

**1. Controller retorna entidade JPA diretamente (expõe senha hash):**
```java
// UserController.java — linha 21
public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    User user = userService.findByEmail(email);
    return ResponseEntity.ok(user); // serializa User com campo `senha` (BCrypt hash)
}
```

**2. Controller injeta Repository diretamente (viola separação de camadas):**
```java
// PropostaController.java — linha 28
public PropostaController(PropostaService propostaService, PrestadorRepository prestadorRepository) {
    this.prestadorRepository = prestadorRepository; // Repository no Controller
}
```

**3. Templates HTML inline em componentes grandes:**
```typescript
// prestador-dashboard.component.ts — 243 linhas de TS + HTML no mesmo arquivo
// cliente-dashboard.component.ts — 409 linhas de TS + HTML no mesmo arquivo
```

**4. Método de busca de perfil incorreto ainda presente em `prestador-perfil.component.ts`:**
```typescript
// prestador-perfil.component.ts — linha 237
this.prestadorService.buscarPorUsuario(userId).subscribe({  // endpoint /usuario/{id} não existe
```

**5. Botão sem handler — ação morta:**
```typescript
// cliente-dashboard.component.ts — linha 376-379
editarSolicitacao(): void {
    if (!this.solicitacaoSelecionada) return;
    this.fecharModal(); // apenas fecha o modal, sem navegar ou editar
}
```

---

## 3. ENTIDADES E DOMÍNIO

### Entidades JPA existentes

#### `User` → tabela `usuarios`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| nome | String | NOT NULL |
| login | String | NOT NULL, UNIQUE |
| email | String | NOT NULL, UNIQUE |
| senha | String | NOT NULL (BCrypt) |
| tipo | TipoUsuario (enum) | NOT NULL |
| telefone | String | nullable |
| cpf | String | nullable |
| rg | String | nullable |
| dataExpedicao | String | nullable |
| dataCadastro | LocalDateTime | NOT NULL, auto |
| ativo | Boolean | NOT NULL, default true |

#### `Prestador` → tabela `prestadores`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| usuario | User | FK, NOT NULL, UNIQUE (OneToOne) |
| categorias | List\<Categoria\> | ManyToMany via `prestador_categorias` |
| descricaoPerfil | String | TEXT, nullable |
| cidade | String | nullable |
| estado | String | nullable |
| avaliacaoMedia | Double | NOT NULL, default 0.0 |
| totalAvaliacoes | Integer | NOT NULL, default 0 |

#### `Categoria` → tabela `categorias`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| nome | String | NOT NULL, UNIQUE |
| descricao | String | TEXT, nullable |

#### `SolicitacaoServico` → tabela `solicitacoes_servico`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| cliente | User | FK, NOT NULL (ManyToOne) |
| categoria | Categoria | FK, NOT NULL (ManyToOne) |
| descricao | String | TEXT, NOT NULL |
| cidade | String | NOT NULL |
| estado | String | NOT NULL |
| status | StatusSolicitacao (enum) | NOT NULL, default ABERTA |
| dataCriacao | LocalDateTime | NOT NULL, auto |

#### `Proposta` → tabela `propostas`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| solicitacao | SolicitacaoServico | FK, NOT NULL (ManyToOne) |
| prestador | Prestador | FK, NOT NULL (ManyToOne) |
| valor | BigDecimal | NOT NULL, precision(10,2) |
| mensagem | String | TEXT, nullable |
| status | StatusProposta (enum) | NOT NULL, default ENVIADA |
| dataEnvio | LocalDateTime | NOT NULL, auto |

#### `Avaliacao` → tabela `avaliacoes`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| solicitacao | SolicitacaoServico | FK, NOT NULL (ManyToOne) |
| prestador | Prestador | FK, NOT NULL (ManyToOne) |
| cliente | User | FK, NOT NULL (ManyToOne) |
| nota | Integer | NOT NULL |
| comentario | String | TEXT, nullable |

### Relacionamentos entre entidades

```
User (CLIENTE) ──────────────── SolicitacaoServico (N)
                                      │
User (PRESTADOR) ── Prestador ── Proposta (N)
                        │              │
                   Categoria (N)    [aceita]
                                      │
                               Avaliacao ──── Prestador
                                        └──── User (CLIENTE)
```

### O que está faltando no domínio

| Entidade/Conceito | Motivo da ausência | Impacto |
|-------------------|--------------------|---------|
| **Contrato** | Não há entidade formal de contrato quando proposta é aceita | Alto — sem rastreabilidade pós-aceite |
| **Notificacao** | Sem sistema de notificações | Alto — usuário não sabe de novos eventos |
| **Pagamento** | Sem modelo de pagamento integrado | Alto — marketplace sem monetização |
| **dataCriacao em Avaliacao** | Campo ausente na entidade | Médio — sem ordenação cronológica |
| **orcamento em SolicitacaoServico** | Cliente não informa budget | Médio — prestador propõe sem referência |
| **foto/portfolio em Prestador** | Sem suporte a imagens | Médio — perfil menos atrativo |
| **Role ADMIN** | `TipoUsuario` tem apenas CLIENTE e PRESTADOR | Alto — qualquer autenticado gerencia categorias |

---

## 4. ENDPOINTS EXISTENTES

### `/api/auth`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| POST | `/api/auth/register` | Pública | Registra usuário e retorna JWT | RegisterDTO exige CPF/RG/dataExpedicao com `@NotBlank`, mas o formulário de cadastro do frontend não envia esses campos → 400 na maioria dos cadastros |
| POST | `/api/auth/login` | Pública | Autentica e retorna JWT | Sem rate limiting — vulnerável a brute force |

### `/api/categorias`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| GET | `/api/categorias` | Pública | Lista categorias (paginado) | — |
| GET | `/api/categorias/{id}` | Pública | Busca categoria por ID | — |
| POST | `/api/categorias` | Qualquer autenticado | Cria categoria | Deveria exigir ADMIN; qualquer usuário logado pode criar |
| PUT | `/api/categorias/{id}` | Qualquer autenticado | Atualiza categoria | Mesmo problema acima |
| DELETE | `/api/categorias/{id}` | Qualquer autenticado | Deleta categoria | Mesmo problema acima |

### `/api/prestadores`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| GET | `/api/prestadores` | Pública | Busca prestadores com filtros | — |
| GET | `/api/prestadores/{id}` | Pública | Busca prestador por ID | — |
| GET | `/api/prestadores/me` | JWT | Retorna perfil do prestador logado | — |
| POST | `/api/prestadores` | JWT | Cria ou atualiza perfil | Qualquer usuário CLIENTE pode criar perfil de prestador (sem verificação de tipo) |

### `/api/solicitacoes`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| POST | `/api/solicitacoes` | JWT | Cria solicitação | Sem verificação de que o usuário é CLIENTE |
| GET | `/api/solicitacoes/{id}` | JWT | Busca solicitação por ID | — |
| GET | `/api/solicitacoes/minhas` | JWT | Lista solicitações do cliente logado | — |
| GET | `/api/solicitacoes/abertas` | JWT | Lista todas as solicitações ABERTAS | — |
| PATCH | `/api/solicitacoes/{id}/status` | JWT | Muda status da solicitação | **Sem verificação de ownership** — qualquer usuário logado pode mudar status de qualquer solicitação |
| DELETE | `/api/solicitacoes/{id}` | JWT | Cancela (deleta) solicitação | Verifica ownership corretamente |

### `/api/propostas`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| POST | `/api/propostas` | JWT | Envia proposta | PropostaController injeta `PrestadorRepository` diretamente (violação de camadas); lança `RuntimeException` não tratada se prestador não encontrado |
| GET | `/api/propostas/solicitacao/{id}` | JWT | Lista propostas de uma solicitação | Qualquer autenticado acessa propostas de qualquer solicitação |
| GET | `/api/propostas/minhas` | JWT | Lista propostas do prestador logado | — |
| PATCH | `/api/propostas/{id}/aceitar` | JWT | Aceita proposta | **Sem verificação de que o usuário é dono da solicitação** — qualquer autenticado pode aceitar |
| PATCH | `/api/propostas/{id}/recusar` | JWT | Recusa proposta | Mesmo problema acima |

### `/api/avaliacoes`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| POST | `/api/avaliacoes` | JWT | Cria avaliação | Sem verificação de que a proposta foi concluída antes de avaliar; sem proteção contra avaliação duplicada |
| GET | `/api/avaliacoes/prestador/{id}` | Pública | Lista avaliações de um prestador | — |

### `/api/users`

| Método | Path | Autenticação | O que faz | Problemas |
|--------|------|-------------|-----------|-----------|
| GET | `/api/users/me` | JWT | Retorna usuário logado | **Retorna entidade `User` diretamente com campo `senha` (hash BCrypt) serializado no JSON** |
| PUT | `/api/users/me` | JWT | Atualiza dados do usuário | Aceita `User` entity no body — permite alterar qualquer campo incluindo `tipo` e `senha` sem validação |
| DELETE | `/api/users/me` | JWT | Soft delete do usuário | — |

---

## 5. FRONTEND

### Componentes existentes

| Componente | Rota | Autenticação | Status | Observações |
|------------|------|-------------|--------|-------------|
| `HomeComponent` | `/` | Pública | ✅ Funcional | Busca e click em categorias apenas `console.log` |
| `LoginComponent` | `/login` | Pública | ✅ Funcional | Usa Angular Material |
| `RegisterComponent` | `/register` | Pública | ⚠️ Quebrado | Cadastro falha pois `RegisterDTO` exige CPF/RG/dataExpedicao mas componente não envia |
| `NavbarComponent` | — | — | ✅ Funcional | — |
| `ClienteDashboardComponent` | `/cliente/dashboard` | clienteGuard | ✅ Funcional | Botão "Editar" no modal apenas fecha o modal (linha 376) |
| `ClienteNovaSolicitacaoComponent` | `/cliente/nova-solicitacao` | clienteGuard | ✅ Funcional | — |
| `PrestadorDashboardComponent` | `/prestador/dashboard` | prestadorGuard | ✅ Funcional | Contadores "Propostas Enviadas" e "Serviços Concluídos" hardcoded em `0`; botão "Ver Detalhes e Enviar Proposta" sem handler |
| `PrestadorPerfilComponent` | `/prestador/perfil` | prestadorGuard | ⚠️ Parcial | `carregarPerfilExistente()` chama `/api/prestadores/usuario/{id}` (endpoint inexistente) — perfil não é carregado ao editar |
| `UserProfileComponent` | `/meu-perfil` | clienteGuard + prestadorGuard | ✅ Funcional | — |

### Rotas configuradas

```typescript
{ path: '',                         component: HomeComponent }
{ path: 'login',                    component: LoginComponent }
{ path: 'register',                 component: RegisterComponent }
{ path: 'cliente/dashboard',        component: ClienteDashboardComponent,        canActivate: [clienteGuard] }
{ path: 'cliente/nova-solicitacao', component: ClienteNovaSolicitacaoComponent,  canActivate: [clienteGuard] }
{ path: 'prestador/dashboard',      component: PrestadorDashboardComponent,      canActivate: [prestadorGuard] }
{ path: 'prestador/perfil',         component: PrestadorPerfilComponent,         canActivate: [prestadorGuard] }
{ path: 'meu-perfil',               component: UserProfileComponent,             canActivate: [clienteGuard, prestadorGuard] }
{ path: '**',                       redirectTo: '' }
```

**Rotas ausentes:** propostas recebidas (cliente), detalhe de proposta (prestador), avaliação, painel admin.

### Serviços Angular e integrações

| Service | Base URL | Endpoints consumidos |
|---------|----------|---------------------|
| `AuthService` | `http://localhost:8080/api/auth` | POST /register, POST /login |
| `CategoriaService` | `http://localhost:8080/api/categorias` | GET / (paginado), GET /{id} |
| `SolicitacaoService` | `http://localhost:8080/api/solicitacoes` | POST /, GET /minhas, GET /{id}, GET /abertas, DELETE /{id} |
| `PrestadorService` | `http://localhost:8080/api/prestadores` | GET /, GET /me, POST /, PUT /{id} |
| `PropostaService` | `http://localhost:8080/api/propostas` | Declarado mas **não usado por nenhum componente** |
| `AvaliacaoService` | `http://localhost:8080/api/avaliacoes` | Declarado mas **não usado por nenhum componente** |
| `UserService` | `http://localhost:8080/api/users` | GET /me, PUT /me |

### Violações dos padrões Angular

| Violação | Onde | Impacto |
|----------|------|---------|
| Todos os URLs do backend hardcoded em `http://localhost:8080` | Todos os services | Impossível fazer deploy sem recompilar |
| Templates HTML inline nos arquivos `.ts` | Todos os componentes | Legibilidade ruim; componentes com 400+ linhas |
| Sem interceptor de resposta para 401 | — | Token expirado não redireciona ao login |
| `isAuthenticated()` verifica apenas presença do token no localStorage, não validade | `auth.service.ts` | Token expirado ainda passa nos guards |
| `meu-perfil` com `canActivate: [clienteGuard, prestadorGuard]` | `app.routes.ts` | Os dois guards rodam em sequência; prestador é bloqueado pelo `clienteGuard` antes de `prestadorGuard` verificar |

---

## 6. DÉBITOS TÉCNICOS

### CRÍTICO

| # | Problema | Arquivo | Impacto |
|---|----------|---------|---------|
| C1 | `UserController` retorna entidade `User` com campo `senha` (hash BCrypt) serializado no JSON | `UserController.java:21` | Exposição de hash de senha em toda chamada a `/api/users/me` |
| C2 | `PATCH /api/propostas/{id}/aceitar` e `/recusar` sem verificação de ownership | `PropostaController.java:64-74` | Qualquer usuário autenticado aceita/recusa propostas de outros |
| C3 | `PATCH /api/solicitacoes/{id}/status` sem verificação de ownership | `SolicitacaoServicoController.java:64-70` | Qualquer usuário autenticado muda status de qualquer solicitação |
| C4 | `DataSeeder` sem profile guard — cria usuários com senha `123456` em produção | `DataSeeder.java` | Backdoor em produção |
| C5 | Cadastro de novos usuários quebrado — `RegisterDTO` exige CPF/RG/dataExpedicao obrigatórios, frontend não envia | `RegisterDTO.java:26-38` + `register.component.ts` | Nenhum novo usuário consegue se cadastrar via frontend |

### ALTO

| # | Problema | Arquivo | Impacto |
|---|----------|---------|---------|
| A1 | CORS hardcoded em `http://localhost:4200` | `SecurityConfig.java:63` | Deploy impossível sem recompilar |
| A2 | URL do backend hardcoded em `http://localhost:8080` em todos os services Angular | Todos os `services/*.ts` | Deploy impossível sem recompilar |
| A3 | Swagger exposto publicamente sem restrição | `SecurityConfig.java:49` | Exposição total da API em produção |
| A4 | `POST /api/categorias`, `PUT`, `DELETE` sem restrição de role — qualquer autenticado gerencia categorias | `CategoriaController.java:39-58` | Usuários comuns deletam categorias do sistema |
| A5 | `POST /api/prestadores` sem verificação de tipo — CLIENTE pode criar perfil de PRESTADOR | `PrestadorController.java:52-60` | Violação de regra de negócio central |
| A6 | `PUT /api/users/me` aceita entidade `User` no body — permite alterar `tipo`, `login` e outros campos críticos | `UserController.java:27-35` | Escalada de privilégios (CLIENTE promovendo-se a PRESTADOR) |
| A7 | Sem interceptor de resposta 401 no Angular | — | Usuário com token expirado fica preso sem feedback |
| A8 | `prestador-perfil.component.ts` usa `buscarPorUsuario(userId)` (endpoint inexistente) ao editar perfil | `prestador-perfil.component.ts:237` | Formulário de edição nunca pré-preenche os dados existentes |

### MÉDIO

| # | Problema | Arquivo | Impacto |
|---|----------|---------|---------|
| M1 | `PropostaController` injeta `PrestadorRepository` diretamente | `PropostaController.java:28` | Violação de camadas; lógica de negócio no controller |
| M2 | `POST /api/avaliacoes` sem validação de que a proposta foi concluída | `AvaliacaoService.java` | Cliente avalia antes do serviço ser realizado |
| M3 | Sem proteção contra avaliação duplicada | `AvaliacaoService.java` | Cliente avalia o mesmo serviço múltiplas vezes |
| M4 | JWT expira em 24h sem refresh token | `JwtTokenProvider.java` | Usuário deslogado sem aviso |
| M5 | `isAuthenticated()` não valida expiração do JWT — apenas verifica presença no localStorage | `auth.service.ts:63-65` | Guards passam com token expirado |
| M6 | `canActivate: [clienteGuard, prestadorGuard]` em `/meu-perfil` bloqueia prestadores | `app.routes.ts:38-40` | Prestador não consegue acessar próprio perfil |
| M7 | Contadores "Propostas Enviadas" e "Serviços Concluídos" hardcoded em `0` | `prestador-dashboard.component.ts:71,92` | Informação sempre falsa |
| M8 | Botão "Ver Detalhes e Enviar Proposta" sem `(click)` handler | `prestador-dashboard.component.ts:171-174` | Ação central do prestador completamente inoperante |
| M9 | Botão "Editar" na modal do cliente apenas fecha o modal | `cliente-dashboard.component.ts:376-379` | Ação prometida não executada |
| M10 | `ddl-auto: update` sem controle de migração (sem Flyway/Liquibase) | `application.yml:13` | Sem rastreabilidade de mudanças no schema |
| M11 | `Avaliacao` sem campo `dataCriacao` | `Avaliacao.java` | Impossível ordenar avaliações cronologicamente |

### BAIXO

| # | Problema | Arquivo | Impacto |
|---|----------|---------|---------|
| B1 | Sem rate limiting em `POST /api/auth/login` | `SecurityConfig.java` | Vulnerável a brute force |
| B2 | `PropostaService` e `AvaliacaoService` declarados no Angular mas não usados por nenhum componente | `proposta.service.ts`, `avaliacao.service.ts` | Dead code |
| B3 | `buscarServico()` e `selecionarCategoria()` na home apenas fazem `console.log` | `home.component.ts:234-238` | Funcionalidade prometida sem implementação |
| B4 | Sem paginação visível no frontend (backend pagina, frontend ignora controles) | Todos os dashboards | Só os primeiros registros aparecem |
| B5 | Erros de HTTP tratados apenas com `console.error()` sem feedback visual ao usuário | Múltiplos componentes | Falhas silenciosas para o usuário |
| B6 | Templates HTML inline em arquivos TypeScript — componentes com 400+ linhas | Todos os componentes | Manutenção difícil |

---

## 7. O QUE FALTA IMPLEMENTAR

Ordenado por prioridade de negócio para que o marketplace seja utilizável.

### P1 — Desbloqueio do fluxo central

| Feature | Descrição | Entidades | Endpoints necessários | Esforço |
|---------|-----------|-----------|----------------------|---------|
| **Tela de detalhe e envio de proposta (prestador)** | Prestador clica em "Ver Detalhes", vê a solicitação completa e envia proposta com valor e mensagem | Proposta, SolicitacaoServico | `POST /api/propostas` (já existe) | **P** |
| **Tela de propostas recebidas (cliente)** | Cliente vê todas as propostas recebidas por solicitação, com valor, nome e avaliação do prestador | Proposta, Prestador | `GET /api/propostas/solicitacao/{id}` (já existe) | **M** |
| **Aceitar/rejeitar proposta (cliente)** | Botões de aceite e rejeição integrados na tela acima | Proposta, SolicitacaoServico | `PATCH /api/propostas/{id}/aceitar`, `/recusar` (já existe) | **P** |
| **Marcar serviço como concluído** | Após aceitar proposta, cliente ou prestador marca como concluído | SolicitacaoServico | `PATCH /api/solicitacoes/{id}/status` (já existe) | **P** |
| **Tela de avaliação** | Após serviço concluído, cliente avalia o prestador com nota e comentário | Avaliacao | `POST /api/avaliacoes` (já existe) | **P** |

### P2 — Correções obrigatórias antes de qualquer uso real

| Feature | Descrição | Esforço |
|---------|-----------|---------|
| **Corrigir cadastro de usuário** | Tornar CPF/RG/dataExpedicao opcionais ou adicionar campos no formulário | **P** |
| **Variáveis de ambiente no frontend** | Substituir `http://localhost:8080` por `environment.apiUrl` em todos os services | **P** |
| **CORS configurável** | Ler `allowedOrigins` de variável de ambiente | **P** |
| **Interceptor de 401** | Redirecionar ao login quando token expirar | **P** |
| **DTO para UserController** | Criar `UserResponseDTO` sem campo `senha` | **P** |
| **Corrigir prestador-perfil** | Trocar `buscarPorUsuario()` por `buscarMeuPerfil()` (mesmo bug já corrigido no dashboard) | **P** |

### P3 — Segurança antes de produção

| Feature | Descrição | Esforço |
|---------|-----------|---------|
| **Ownership checks** | Verificar dono em PATCH /status, aceitar/recusar proposta | **P** |
| **Role ADMIN** | Adicionar TipoUsuario.ADMIN; proteger endpoints de categoria | **M** |
| **Restrição do Swagger em produção** | Liberar Swagger apenas com profile `dev` | **P** |
| **DataSeeder com profile guard** | Executar seed apenas com profile `dev` ou `test` | **P** |
| **Validação de tipo em criação de recursos** | Verificar que CLIENTE cria solicitação e PRESTADOR envia proposta | **P** |

### P4 — Features de produto completas

| Feature | Descrição | Entidades | Esforço |
|---------|-----------|-----------|---------|
| **Sistema de notificações** | Notificar prestador quando nova solicitação na sua categoria; notificar cliente quando proposta chegar | Notificacao (nova) | **G** |
| **Histórico de serviços** | Prestador vê serviços concluídos com avaliação recebida | Avaliacao, Proposta | **M** |
| **Painel administrativo** | Gerenciar categorias, usuários e ver métricas | Role ADMIN | **G** |
| **Paginação no frontend** | Controles de paginação em todas as listagens | — | **M** |
| **Edição de solicitação** | Implementar `editarSolicitacao()` que navega para formulário de edição | SolicitacaoServico | **M** |
| **Busca por categoria na home** | Conectar barra de busca e cards de categoria à listagem de prestadores | — | **P** |
| **Refresh token** | Renovar JWT automaticamente antes de expirar | — | **M** |
| **Portfolio do prestador** | Upload de fotos de trabalhos anteriores | nova entidade | **G** |
| **Chat entre cliente e prestador** | Mensagens diretas após aceite da proposta | nova entidade | **G** |
| **Pagamento integrado** | Integração com gateway (ex: Stripe, Pagar.me) | Pagamento (nova) | **G** |

---

## 8. PLANO DE AÇÃO SUGERIDO

### Fase 1 — Estabilização (débitos críticos e altos) — ~2 semanas

**Objetivo:** Corrigir o que quebra o sistema ou representa risco de segurança.

```
Sprint 1:
  [C5] Corrigir cadastro: tornar CPF/RG opcionais no RegisterDTO
  [C1] Criar UserResponseDTO e usar em UserController
  [A6] Restringir PUT /api/users/me a campos editáveis pelo usuário
  [C3] Adicionar verificação de ownership em PATCH /solicitacoes/{id}/status
  [C2] Adicionar verificação de ownership em aceitar/recusar proposta
  [A4] Proteger endpoints de categoria com role ADMIN (ou criar role temporariamente)
  [A5] Verificar tipo do usuário em POST /api/prestadores

Sprint 2:
  [A1] CORS via variável de ambiente
  [A2] URLs do backend via environment.ts no Angular
  [A7] Interceptor de resposta 401 no Angular
  [A3] Swagger restrito ao profile dev
  [C4] DataSeeder com @Profile("dev") ou "!prod"
  [A8] Corrigir buscarPorUsuario → buscarMeuPerfil em prestador-perfil.component.ts
  [M6] Corrigir canActivate em /meu-perfil
```

### Fase 2 — Fluxo central (features P1) — ~2 semanas

**Objetivo:** Fazer o marketplace funcionar de ponta a ponta.

```
Sprint 3 (dependências: Fase 1 concluída):
  Tela de detalhe e envio de proposta (prestador)
  Tela de propostas recebidas (cliente) com aceitar/rejeitar
  Marcar serviço como concluído

Sprint 4:
  Tela de avaliação pós-serviço
  Contadores reais no dashboard do prestador
  Paginação nas listagens
  Busca por categoria na home
```

### Fase 3 — Qualidade e produto — ~3 semanas

**Objetivo:** Tornar o sistema confiável e com funcionalidades de produto.

```
Sprint 5:
  Flyway/Liquibase para controle de migrações
  Separar templates HTML em arquivos .html separados
  Testes unitários backend (JUnit 5 + Mockito) — mínimo de services
  Edição de solicitação (implementar editarSolicitacao())

Sprint 6:
  Refresh token
  Sistema de notificações (polling ou WebSocket)
  Histórico de serviços no dashboard do prestador
  Painel administrativo básico
```

### Dependências entre tarefas

```
C5 (cadastro) → qualquer novo usuário de teste
A8 (prestador-perfil) → edição de perfil funcional
A1 + A2 (CORS + env) → deploy em qualquer ambiente
C2 + C3 (ownership) → segurança mínima aceitável
Fase 1 completa → Fase 2 (fluxo central)
Fase 2 Sprint 3 → Sprint 4 (avaliação depende de serviço concluído)
```

---

*Gerado a partir da leitura direta de todos os arquivos do projeto em 2026-05-11.*
