# Levantamento Técnico — VemResolver (YouService Marketplace)

> **Última atualização:** 2026-05-12  
> **Analista:** Claude Sonnet 4.6  
> **Branch analisada:** main / develop (sincronizadas)

---

## 1. VISÃO GERAL

### Nome e propósito
**YouService Marketplace** — plataforma que conecta clientes que precisam de serviços domésticos/profissionais com prestadores autônomos. O fluxo central é: cliente abre solicitação → prestadores enviam propostas com valor → cliente aceita uma proposta → serviço é executado → cliente e prestador avaliam-se mutuamente.

### Stack tecnológico

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
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
| Infraestrutura | Docker + Docker Compose | 29.x |

---

## 2. ARQUITETURA ATUAL

### Estrutura de pastas — Backend

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
│   ├── PerfilController.java    (adicionado — gerencia perfil via DTOs)
│   ├── PrestadorController.java
│   ├── PropostaController.java
│   ├── SolicitacaoServicoController.java
│   └── UserController.java     (legado — retorna entidade User diretamente)
├── dto/
│   ├── AlterarSenhaDTO.java
│   ├── AuthResponseDTO.java
│   ├── AvaliacaoDTO.java
│   ├── CategoriaDTO.java
│   ├── LoginDTO.java
│   ├── PrestadorDTO.java
│   ├── PropostaDTO.java         (enriquecido: dados da solicitação, foiAvaliadoPeloPrestador)
│   ├── RegisterDTO.java
│   ├── SolicitacaoServicoDTO.java (enriquecido: foiAvaliadoPeloCliente, foiAvaliadoPeloPrestador)
│   └── UpdatePerfilDTO.java
├── exception/
│   ├── BusinessException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Avaliacao.java           (campo tipo: TipoAvaliacao adicionado)
│   ├── Categoria.java
│   ├── Prestador.java           (avaliacaoMedia e totalAvaliacoes)
│   ├── Proposta.java
│   ├── SolicitacaoServico.java
│   ├── StatusProposta.java      (enum: ENVIADA, ACEITA, RECUSADA)
│   ├── StatusSolicitacao.java   (enum: ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA)
│   ├── TipoAvaliacao.java       (novo — CLIENTE_AVALIA_PRESTADOR, PRESTADOR_AVALIA_CLIENTE)
│   ├── TipoUsuario.java         (enum: CLIENTE, PRESTADOR)
│   ├── User.java                (enriquecido: endereço, avaliacaoMedia, totalAvaliacoes)
│   └── VerificationToken.java   (novo — token de verificação de e-mail)
├── repository/
│   ├── AvaliacaoRepository.java
│   ├── CategoriaRepository.java
│   ├── PrestadorRepository.java
│   ├── PropostaRepository.java
│   ├── SolicitacaoServicoRepository.java
│   ├── UserRepository.java
│   └── VerificationTokenRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenProvider.java
└── service/
    ├── AuthService.java
    ├── AvaliacaoService.java
    ├── CategoriaService.java
    ├── EmailService.java        (envio de e-mail de verificação)
    ├── PerfilService.java       (novo)
    ├── PrestadorService.java
    ├── PropostaService.java
    ├── SolicitacaoServicoService.java
    └── UserService.java
```

### Estrutura de pastas — Frontend

```
frontend/marketplace-frontend/src/app/
├── components/
│   ├── cliente-avaliacao.component.ts         (/cliente/solicitacao/:id/avaliacao)
│   ├── cliente-dashboard.component.ts
│   ├── cliente-editar-solicitacao.component.ts
│   ├── cliente-nova-solicitacao.component.ts
│   ├── cliente-perfil.component.ts            (/cliente/perfil)
│   ├── cliente-propostas-recebidas.component.ts (/cliente/solicitacao/:id/propostas)
│   ├── cookie-banner.component.ts
│   ├── footer/footer.component.ts
│   ├── global-snack-bar/global-snack-bar.component.ts
│   ├── home/home.component.ts                 (novo — substitui home.component.ts)
│   ├── home.component.ts                      (legado — mantido por compatibilidade)
│   ├── login.component.ts
│   ├── navbar.component.ts
│   ├── notification/notification.component.ts
│   ├── notification-container/notification-container.component.ts
│   ├── notification-demo/notification-demo.component.ts
│   ├── politica-privacidade.component.ts
│   ├── prestador-avaliacao.component.ts       (/prestador/solicitacao/:id/avaliacao)
│   ├── prestador-dashboard.component.ts
│   ├── prestador-enviar-proposta.component.ts (/prestador/solicitacao/:id)
│   ├── prestador-perfil/prestador-perfil.component.ts (/prestador/perfil)
│   ├── register/register.component.ts
│   ├── seguranca.component.ts
│   ├── seja-profissional.component.ts
│   ├── seletor-uf.component.ts
│   ├── sobre-nos.component.ts
│   ├── termos-uso.component.ts
│   ├── user-profile.component.ts              (legado)
│   └── verify-account.component.ts
├── directives/
│   ├── mask.directive.ts
│   └── uppercase.directive.ts
├── guards/
│   └── auth.guard.ts                          (authGuard, clienteGuard, prestadorGuard)
├── interceptors/
│   └── auth.interceptor.ts                    (adiciona JWT no header — sem interceptor de 401)
├── models/
│   └── models.ts
├── services/
│   ├── auth.service.ts
│   ├── avaliacao.service.ts
│   ├── categoria.service.ts
│   ├── notification.service.ts
│   ├── prestador.service.ts
│   ├── proposta.service.ts
│   ├── snack.service.ts
│   ├── solicitacao.service.ts
│   ├── user.service.ts
│   └── viacep.service.ts
└── validators/
    └── custom.validators.ts
```

---

## 3. ENTIDADES E DOMÍNIO

### Entidades JPA

#### `User` → tabela `usuarios`
| Campo | Tipo | Restrições |
|-------|------|-----------|
| id | Long | PK, auto |
| nome | String | NOT NULL |
| login | String | NOT NULL, UNIQUE |
| email | String | NOT NULL, UNIQUE |
| senha | String | NOT NULL (BCrypt); `@JsonProperty(WRITE_ONLY)` — não serializado |
| tipo | TipoUsuario (enum) | NOT NULL |
| telefone | String | nullable |
| cpf | String | nullable |
| rg | String | nullable |
| orgaoExpedidor | String | nullable |
| dataExpedicaoRg | String | nullable (formato DD/MM/AAAA) |
| cep | String | nullable |
| endereco | String | nullable |
| cidade | String | nullable |
| estado | String | nullable |
| numero | String | nullable |
| complemento | String | nullable |
| dataCadastro | LocalDateTime | NOT NULL, auto (TIMESTAMP sem nanos) |
| ativo | Boolean | NOT NULL, default true |
| avaliacaoMedia | Double | NOT NULL, default 0.0 |
| totalAvaliacoes | Integer | NOT NULL, default 0 |

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
| tipo | TipoAvaliacao (enum) | NOT NULL, default CLIENTE_AVALIA_PRESTADOR |
| nota | Integer | NOT NULL |
| comentario | String | TEXT, nullable |

> ⚠️ `Avaliacao` não tem campo `dataCriacao` — impossível ordenar avaliações cronologicamente.

#### `VerificationToken` → tabela `verification_tokens`
Armazena token de verificação de e-mail enviado ao registrar. Usuário só ativa a conta via link no e-mail.

---

## 4. ENDPOINTS EXISTENTES

### `/api/auth`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| POST | `/api/auth/register` | Pública | Registra usuário; envia e-mail de verificação | Retorna 200 com token null (usuário deve verificar e-mail antes de logar) |
| POST | `/api/auth/login` | Pública | Autentica e retorna JWT | ⚠️ Sem rate limiting |
| GET | `/api/auth/verify` | Pública | Verifica conta via token de e-mail | — |
| DELETE | `/api/auth/conta` | JWT | Desativa a própria conta | — |

### `/api/perfil` *(novo)*
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| GET | `/api/perfil` | JWT | Retorna perfil completo do usuário logado | Usa `PerfilService`; anula `senha` antes de retornar |
| PUT | `/api/perfil` | JWT | Atualiza dados editáveis via `UpdatePerfilDTO` | Mais seguro que `PUT /api/users/me` |
| PUT | `/api/perfil/senha` | JWT | Altera senha via `AlterarSenhaDTO` | — |

### `/api/users` *(legado)*
| Método | Path | Auth | O que faz | Problemas |
|--------|------|------|-----------|-----------|
| GET | `/api/users/me` | JWT | Retorna `User` entity | ⚠️ Retorna entidade diretamente (sem DTO); `senha` está `WRITE_ONLY` mas outros campos sensíveis ficam expostos |
| PUT | `/api/users/me` | JWT | Atualiza usuário | ⚠️ Aceita `User` entity no body — permite alterar `tipo`, `login` etc. |
| DELETE | `/api/users/me` | JWT | Soft delete | — |

### `/api/prestadores`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| GET | `/api/prestadores` | Pública | Lista prestadores com filtros (paginado) | — |
| GET | `/api/prestadores/{id}` | Pública | Busca prestador por ID | — |
| GET | `/api/prestadores/me` | JWT | Perfil do prestador logado | — |
| POST | `/api/prestadores` | JWT | Cria ou atualiza perfil de prestador | ⚠️ Sem verificação de que o usuário é PRESTADOR |
| PUT | `/api/prestadores/{id}` | JWT | Atualiza perfil | — |

### `/api/solicitacoes`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| POST | `/api/solicitacoes` | JWT | Cria solicitação | ⚠️ Sem verificação de que o usuário é CLIENTE |
| GET | `/api/solicitacoes/{id}` | JWT | Busca solicitação por ID | — |
| PUT | `/api/solicitacoes/{id}` | JWT | Atualiza solicitação | — |
| GET | `/api/solicitacoes/minhas` | JWT | Lista solicitações do cliente logado | — |
| GET | `/api/solicitacoes/abertas` | JWT | Lista solicitações ABERTAS (para prestadores) | — |
| PATCH | `/api/solicitacoes/{id}/status` | JWT | Muda status manualmente | ⚠️ Sem verificação de ownership |
| PATCH | `/api/solicitacoes/{id}/concluir` | JWT | Marca como CONCLUÍDA | ✅ Verifica que o usuário é o cliente dono; só funciona em EM_ANDAMENTO |
| DELETE | `/api/solicitacoes/{id}` | JWT | Cancela solicitação | ✅ Verifica ownership |

### `/api/propostas`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| POST | `/api/propostas` | JWT | Envia proposta | ⚠️ `PropostaController` injeta `PrestadorRepository` diretamente |
| GET | `/api/propostas/solicitacao/{id}` | JWT | Lista propostas de uma solicitação | ⚠️ Qualquer autenticado acessa propostas de qualquer solicitação |
| GET | `/api/propostas/minhas` | JWT | Propostas do prestador logado | — |
| PATCH | `/api/propostas/{id}/aceitar` | JWT | Aceita proposta | ⚠️ Sem verificação de que o usuário é dono da solicitação |
| PATCH | `/api/propostas/{id}/recusar` | JWT | Recusa proposta | ⚠️ Mesmo problema |

### `/api/avaliacoes`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| POST | `/api/avaliacoes` | JWT | Cliente avalia prestador | ✅ Valida status CONCLUIDA; ✅ unicidade por (solicitacaoId, CLIENTE_AVALIA_PRESTADOR) |
| POST | `/api/avaliacoes/para-cliente` | JWT | Prestador avalia cliente | ✅ Valida proposta ACEITA; ✅ unicidade por (solicitacaoId, PRESTADOR_AVALIA_CLIENTE) |
| GET | `/api/avaliacoes/prestador/{id}` | Pública | Avaliações recebidas por um prestador | — |
| GET | `/api/avaliacoes/cliente/{id}` | Pública | Avaliações recebidas por um cliente | — |

### `/api/categorias`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| GET | `/api/categorias` | Pública | Lista categorias (paginado) | — |
| GET | `/api/categorias/{id}` | Pública | Busca por ID | — |
| POST | `/api/categorias` | Qualquer autenticado | Cria categoria | ⚠️ Deveria exigir ADMIN |
| PUT | `/api/categorias/{id}` | Qualquer autenticado | Atualiza | ⚠️ Mesmo problema |
| DELETE | `/api/categorias/{id}` | Qualquer autenticado | Deleta | ⚠️ Mesmo problema |

---

## 5. FRONTEND

### Componentes e rotas

| Componente | Rota | Auth | Status |
|------------|------|------|--------|
| `HomeComponent` | `/` | Pública | ✅ Funcional |
| `LoginComponent` | `/login` | Pública | ✅ Funcional |
| `RegisterComponent` | `/register` | Pública | ✅ Funcional — stepper 3 etapas, máscara DD/MM/AAAA, btoa(senha) |
| `VerifyAccountComponent` | `/verificar-conta` | Pública | ✅ Funcional |
| `TermosUsoComponent` | `/termos-uso` | Pública | ✅ Informativo |
| `PoliticaPrivacidadeComponent` | `/politica-privacidade` | Pública | ✅ Informativo |
| `SejaProfissionalComponent` | `/seja-um-profissional` | Pública | ✅ Informativo |
| `SegurancaComponent` | `/seguranca` | Pública | ✅ Informativo |
| `SobreNosComponent` | `/sobre-nos` | Pública | ✅ Informativo |
| `ClienteDashboardComponent` | `/cliente/dashboard` | clienteGuard | ✅ Funcional — lista solicitações, modal de detalhes, botão "Concluir", botão "Avaliar" |
| `ClienteNovaSolicitacaoComponent` | `/cliente/nova-solicitacao` | clienteGuard | ✅ Funcional |
| `ClienteEditarSolicitacaoComponent` | `/cliente/editar-solicitacao/:id` | clienteGuard | ⚠️ Existente — verificar se totalmente implementado |
| `ClientePerfilComponent` | `/cliente/perfil` | clienteGuard | ✅ Funcional — edição de dados pessoais e endereço; aba "Minhas Avaliações" com média agregada |
| `ClientePropostasRecebidasComponent` | `/cliente/solicitacao/:id/propostas` | clienteGuard | ✅ Funcional — aceitar/recusar com confirmação |
| `ClienteAvaliacaoComponent` | `/cliente/solicitacao/:id/avaliacao` | clienteGuard | ✅ Funcional — estrelas 1–5, comentário, estados completos |
| `PrestadorDashboardComponent` | `/prestador/dashboard` | prestadorGuard | ✅ Funcional — seções Contratos/Propostas/Concluídos/Oportunidades; stats reais; badge "Avaliado ✓" |
| `PrestadorPerfilComponent` | `/prestador/perfil` | prestadorGuard | ✅ Funcional — pré-preenche via `buscarMeuPerfil()` |
| `PrestadorEnviarPropostaComponent` | `/prestador/solicitacao/:id` | prestadorGuard | ✅ Funcional — detalhes da solicitação + formulário de proposta |
| `PrestadorAvaliacaoComponent` | `/prestador/solicitacao/:id/avaliacao` | prestadorGuard | ✅ Funcional — estrelas 1–5, comentário, estados completos |

### Serviços Angular

| Service | Base URL | Status |
|---------|----------|--------|
| `AuthService` | `http://localhost:8080/api/auth` | ⚠️ URL hardcoded |
| `CategoriaService` | `http://localhost:8080/api/categorias` | ⚠️ URL hardcoded |
| `SolicitacaoService` | `http://localhost:8080/api/solicitacoes` | ⚠️ URL hardcoded |
| `PrestadorService` | `http://localhost:8080/api/prestadores` | ⚠️ URL hardcoded |
| `PropostaService` | `http://localhost:8080/api/propostas` | ⚠️ URL hardcoded |
| `AvaliacaoService` | `http://localhost:8080/api/avaliacoes` | ⚠️ URL hardcoded |
| `UserService` | `http://localhost:8080/api/users` | ⚠️ URL hardcoded |
| `ViaCEPService` | `https://viacep.com.br` | ✅ Busca endereço por CEP |
| `NotificationService` | — | ✅ Toasts e notificações em-tela |

---

## 6. DÉBITOS TÉCNICOS

### CRÍTICO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| C1 | `UserController` retorna entidade `User` diretamente (sem DTO) | `UserController.java` | ⚠️ **Parcial** — `senha` tem `@JsonProperty(WRITE_ONLY)` e não é serializada, mas arquitetura ainda viola separação de camadas |
| C2 | `PATCH /api/propostas/{id}/aceitar` e `/recusar` sem verificação de ownership | `PropostaController.java` | ❌ **Pendente** |
| C3 | `PATCH /api/solicitacoes/{id}/status` sem verificação de ownership | `SolicitacaoServicoController.java` | ❌ **Pendente** — `/concluir` foi corrigido, mas `/status` genérico ainda está aberto |
| C4 | `DataSeeder` sem profile guard — cria usuários com senha `123456` em todos os ambientes | `DataSeeder.java` | ❌ **Pendente** |
| C5 | Cadastro de usuário retornava 400 — `@Size`/`@Pattern` em `RegisterDTO.senha` validavam a string Base64 | `RegisterDTO.java` | ✅ **Resolvido** em 2026-05-12 |

### ALTO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| A1 | CORS hardcoded em `http://localhost:4200` | `SecurityConfig.java` + `PerfilController.java` | ❌ **Pendente** |
| A2 | URL do backend hardcoded `http://localhost:8080` em todos os services Angular | Todos `services/*.ts` | ❌ **Pendente** |
| A3 | Swagger exposto publicamente sem restrição | `SecurityConfig.java` | ❌ **Pendente** |
| A4 | Endpoints de categoria sem restrição de role ADMIN | `CategoriaController.java` | ❌ **Pendente** |
| A5 | `POST /api/prestadores` sem verificação de tipo — CLIENTE pode criar perfil de PRESTADOR | `PrestadorController.java` | ❌ **Pendente** |
| A6 | `PUT /api/users/me` aceita entidade `User` no body — permite alterar `tipo`, `login` etc. | `UserController.java` | ❌ **Pendente** — `PUT /api/perfil` mais seguro existe, mas `/api/users/me` ainda está ativo |
| A7 | Sem interceptor de resposta 401 no Angular | — | ❌ **Pendente** |
| A8 | Formulário de perfil do prestador nunca pré-prenchia os campos | `prestador-perfil.component.ts` | ✅ **Resolvido** em 2026-05-12 |

### MÉDIO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| M1 | `PropostaController` injeta `PrestadorRepository` diretamente | `PropostaController.java` | ❌ **Pendente** |
| M2 | `POST /api/avaliacoes` sem validação de que o serviço foi concluído | `AvaliacaoService.java` | ✅ **Resolvido** — valida status CONCLUIDA |
| M3 | Sem proteção contra avaliação duplicada | `AvaliacaoService.java` | ✅ **Resolvido** — unicidade por (solicitacaoId, TipoAvaliacao) |
| M4 | JWT expira em 24h sem refresh token | `JwtTokenProvider.java` | ❌ **Pendente** |
| M5 | `isAuthenticated()` não valida expiração do JWT | `auth.service.ts` | ❌ **Pendente** |
| M6 | `canActivate: [clienteGuard, prestadorGuard]` em `/meu-perfil` bloqueava prestadores | `app.routes.ts` | ✅ **Resolvido** — rota `/meu-perfil` substituída por `/cliente/perfil` e `/prestador/perfil` separadas |
| M7 | Contadores no dashboard do prestador hardcoded em `0` | `prestador-dashboard.component.ts` | ✅ **Resolvido** — stats reais |
| M8 | Botão "Ver Detalhes e Enviar Proposta" sem handler | `prestador-dashboard.component.ts` | ✅ **Resolvido** — navega para `/prestador/solicitacao/:id` |
| M9 | Botão "Editar" na modal do cliente apenas fecha o modal | `cliente-dashboard.component.ts` | ❌ **Pendente** |
| M10 | `ddl-auto: update` sem controle de migração (sem Flyway/Liquibase) | `application.yml` | ❌ **Pendente** |
| M11 | `Avaliacao` sem campo `dataCriacao` | `Avaliacao.java` | ❌ **Pendente** |

### BAIXO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| B1 | Sem rate limiting em `POST /api/auth/login` | `SecurityConfig.java` | ❌ **Pendente** |
| B2 | `PropostaService` e `AvaliacaoService` declarados mas não usados | — | ✅ **Resolvido** — ambos são usados pelos novos componentes |
| B3 | `buscarServico()` e `selecionarCategoria()` na home apenas fazem `console.log` | `home.component.ts` | ❌ **Pendente** |
| B4 | Sem paginação visível no frontend | Todos os dashboards | ❌ **Pendente** |
| B5 | Erros de HTTP tratados apenas com `console.error()` sem feedback visual | Múltiplos componentes | ⚠️ **Parcial** — novos componentes usam `NotificationService`; legados ainda usam `console.error` |
| B6 | Templates HTML inline em arquivos TypeScript | Todos os componentes originais | ❌ **Pendente** — novos componentes usam arquivos separados |

---

## 7. O QUE FOI IMPLEMENTADO (desde o levantamento original de 2026-05-11)

### Fluxo central — entregue

| Feature | Branch | Entregue |
|---------|--------|---------|
| Infraestrutura Docker corrigida (variáveis de ambiente) | — | 2026-05-11 |
| Tela de envio de proposta (prestador) | `feature/envio-proposta-prestador` | 2026-05-11 |
| Propostas recebidas + aceitar/recusar (cliente) | `feature/propostas-recebidas-cliente` | 2026-05-11 |
| Marcar serviço como CONCLUÍDO + endpoint dedicado `/concluir` | `feature/marcar-servico-concluido` | 2026-05-12 |
| Tela de avaliação do prestador (cliente avalia) | `feature/tela-avaliacao` | 2026-05-12 |
| Avaliação bidirecional (prestador avalia cliente) | `feature/avaliacao-bidirecional` | 2026-05-12 |
| Painel do prestador com contratos em andamento e stats reais | `feature/prestador-contratos-andamento` | 2026-05-12 |

### Bugs corrigidos

| Bug | Branch | Corrigido |
|-----|--------|-----------|
| Banner "complete seu perfil" não desaparecia após salvar | `fix/perfil-prestador-banner` | 2026-05-11 |
| CORS sem método PATCH — 403 em preflight | `fix/cors-patch-method` | 2026-05-12 |
| Unicidade de avaliação e visibilidade de badges "Avaliado ✓" | `fix/avaliacao-regras-visibilidade` | 2026-05-12 |
| Formulário de perfil do prestador nunca pré-prenchia | — | 2026-05-12 |
| Overflow horizontal nos painéis do cliente e do prestador | — | 2026-05-12 |
| Botão "Ver Propostas" exibido indevidamente em serviços concluídos/cancelados | — | 2026-05-12 |
| Aba "Minhas Avaliações" no perfil do cliente exibia avaliações de prestadores | — | 2026-05-12 |
| Cadastro de novos usuários retornando 400 (validação Base64 errada) | — | 2026-05-12 |
| Campo data de expedição com `type="date"` exigia YYYY-MM-DD | — | 2026-05-12 |

---

## 8. O QUE FALTA IMPLEMENTAR

### P1 — Segurança e deploy (obrigatório antes de qualquer produção)

| # | Tarefa | Esforço |
|---|--------|---------|
| P | Centralizar URL do backend em `environment.ts` (Bug #6) | Pequeno |
| P | CORS via variável de ambiente — remover hardcode (Bug #7) | Pequeno |
| P | Interceptor Angular para 401 — redirecionar ao login (Bug #8) | Pequeno |
| P | Ownership checks em `PATCH /propostas/:id/aceitar` e `/recusar` | Pequeno |
| P | Ownership checks em `PATCH /solicitacoes/:id/status` | Pequeno |
| P | `DataSeeder` com `@Profile("dev")` — não rodar em produção | Pequeno |
| P | Swagger restrito ao profile `dev` | Pequeno |
| M | Role ADMIN — proteger endpoints de categoria | Médio |

### P2 — Qualidade de código

| # | Tarefa | Esforço |
|---|--------|---------|
| P | Criar `UserResponseDTO` e usar em `UserController` — remover retorno de entidade | Pequeno |
| P | Mover lógica de `PropostaController` para `PropostaService` (remover `PrestadorRepository` do controller) | Pequeno |
| P | Migrar `PUT /api/users/me` para usar DTO em vez de entidade `User` | Pequeno |
| M | Flyway ou Liquibase para controle de migrações de schema | Médio |
| M | Adicionar `dataCriacao` em `Avaliacao` | Pequeno |

### P3 — UX e produto

| # | Tarefa | Esforço |
|---|--------|---------|
| P | Implementar `editarSolicitacao()` no dashboard do cliente (atualmente só fecha o modal) | Pequeno |
| P | Busca por categoria e barra de pesquisa na home (atualmente só `console.log`) | Médio |
| M | Paginação visível no frontend para todas as listagens | Médio |
| M | Refresh token — renovar JWT antes de expirar | Médio |
| G | Sistema de notificações (polling ou WebSocket) | Grande |
| G | Painel administrativo básico | Grande |

---

## 9. PLANO DE AÇÃO SUGERIDO

### Fase 1 — Preparação para produção (~1 semana)
```
[A2] environment.ts com URL do backend
[A1] CORS via variável de ambiente
[A7] Interceptor 401
[C2] Ownership em aceitar/recusar proposta
[C3] Ownership em PATCH /status
[C4] DataSeeder com @Profile("dev")
[A3] Swagger restrito ao profile dev
[A4] Endpoints de categoria com role ADMIN
```

### Fase 2 — Qualidade de código (~1 semana)
```
[C1] UserResponseDTO em UserController
[A6] PUT /users/me com DTO adequado
[M1] Remover PrestadorRepository do PropostaController
[M10] Flyway para controle de migrações
[M11] dataCriacao em Avaliacao
```

### Fase 3 — UX e produto (~2 semanas)
```
[M9] editarSolicitacao() no dashboard do cliente
[B3] Busca por categoria na home
[B4] Paginação no frontend
[M4] Refresh token
[B1] Rate limiting no login
```

---

*Atualizado em 2026-05-12 com base na leitura direta de todos os arquivos do projeto.*
