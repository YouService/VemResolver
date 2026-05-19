# Levantamento Técnico — VemResolver (YouService Marketplace)

> **Última atualização:** 2026-05-19 (feature/p4.6-badge-verificado: badge automático para prestadores verificados)  
> **Analista:** Claude Sonnet 4.6  
> **Branch analisada:** feature/p4.6-badge-verificado → main / develop

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
│   ├── DataSeeder.java          (seed de categorias, usuários, proposta e chat de teste)
│   ├── DotenvConfig.java        (carrega .env)
│   ├── EncodingFilter.java      (filtro UTF-8)
│   ├── OpenApiConfig.java       (configuração Swagger)
│   ├── SecurityConfig.java      (Spring Security + CORS)
│   └── WebConfig.java           (serve /uploads/** como static resource)
├── controller/
│   ├── AdminController.java     (GET /api/admin/stats, GET /api/admin/usuarios, GET /api/admin/usuarios/online, GET /api/admin/solicitacoes, PATCH /api/admin/solicitacoes/{id}/status — ROLE_ADMIN)
│   ├── AuthController.java
│   ├── AvaliacaoController.java
│   ├── CategoriaController.java
│   ├── ChatController.java      (GET/POST /api/chat/proposta/{id})
│   ├── FotoPrestadorController.java (POST/DELETE /api/prestadores/me/foto-perfil; POST/DELETE /api/prestadores/me/fotos)
│   ├── NotificacaoController.java   (5 endpoints de notificações)
│   ├── PerfilController.java    (gerencia perfil via DTOs)
│   ├── PrestadorController.java (inclui /me/completude e /{id}/publico)
│   ├── PropostaController.java
│   └── SolicitacaoServicoController.java
├── dto/
│   ├── AlterarSenhaDTO.java
│   ├── AuthResponseDTO.java
│   ├── AvaliacaoDTO.java
│   ├── CategoriaDTO.java
│   ├── CompletudePrestadorDTO.java  (percentual + List<ItemChecklist>)
│   ├── LoginDTO.java
│   ├── MensagemChatDTO.java
│   ├── PerfilPublicoDTO.java        (sem dados sensíveis; inclui AvaliacaoResumo interno)
│   ├── PrestadorDTO.java            (inclui fotoPerfilUrl, percentualCompletude, verificado)
│   ├── PropostaDTO.java
│   ├── RegisterDTO.java
│   ├── SolicitacaoAdminDTO.java     (id, clienteNome, clienteEmail, categoriaNome, descricao, cidade, estado, status, dataCriacao, totalPropostas)
│   ├── SolicitacaoServicoDTO.java
│   └── UpdatePerfilDTO.java
├── exception/
│   ├── BusinessException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Avaliacao.java           (tipo: TipoAvaliacao, dataCriacao: @CreationTimestamp)
│   ├── Categoria.java
│   ├── FotoPrestador.java       (id, prestador, url, dataUpload)
│   ├── MensagemChat.java        (id, proposta, remetente, texto, dataEnvio, lida)
│   ├── Notificacao.java
│   ├── Prestador.java           (fotoPerfilUrl, percentualCompletude, latitude, longitude, verificado)
│   ├── Proposta.java
│   ├── RefreshToken.java
│   ├── SolicitacaoServico.java
│   ├── StatusProposta.java      (ENVIADA, ACEITA, RECUSADA)
│   ├── StatusSolicitacao.java   (ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA)
│   ├── TipoAvaliacao.java       (CLIENTE_AVALIA_PRESTADOR, PRESTADOR_AVALIA_CLIENTE)
│   ├── TipoUsuario.java         (CLIENTE, PRESTADOR, ADMIN)
│   ├── User.java
│   └── VerificationToken.java
├── repository/
│   ├── AvaliacaoRepository.java     (inclui findTop5 para perfil público)
│   ├── CategoriaRepository.java
│   ├── FotoPrestadorRepository.java
│   ├── MensagemChatRepository.java
│   ├── NotificacaoRepository.java
│   ├── PrestadorRepository.java
│   ├── PropostaRepository.java      (inclui countByPrestadorIdAndStatusAndSolicitacaoStatus)
│   ├── RefreshTokenRepository.java
│   ├── SolicitacaoServicoRepository.java
│   ├── UserRepository.java          (inclui atualizarUltimoAcesso, findOnline, countByUltimoAcessoAfterAndAtivoTrue)
│   └── VerificationTokenRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   └── OnlineTrackingFilter.java    (não @Component — registrado via SecurityConfig.addFilterAfter; throttle 2 min)
└── service/
    ├── AuthService.java
    ├── AvaliacaoService.java
    ├── CategoriaService.java
    ├── ChatService.java
    ├── EmailService.java
    ├── FotoPrestadorService.java    (@Lazy em PrestadorService para evitar dependência circular)
    ├── NotificacaoService.java
    ├── PerfilService.java
    ├── PrestadorService.java        (getCompletude, recalcularCompletude, recalcularVerificado, buscarPerfilPublico)
    ├── PropostaService.java
    ├── RefreshTokenService.java
    ├── SolicitacaoServicoService.java
    └── UserService.java
```

### Estrutura de pastas — Frontend

```
frontend/marketplace-frontend/src/app/
├── components/
│   ├── admin-dashboard/admin-dashboard.component.ts (/admin/dashboard — adminGuard)
│   ├── busca-prestadores/busca-prestadores.component.ts (/buscar-prestadores)
│   ├── chat/chat.component.ts                 (/chat/proposta/:id — polling 3s)
│   ├── cliente-avaliacao.component.ts         (/cliente/solicitacao/:id/avaliacao)
│   ├── cliente-dashboard.component.ts
│   ├── cliente-editar-solicitacao.component.ts
│   ├── cliente-nova-solicitacao.component.ts
│   ├── cliente-perfil.component.ts            (/cliente/perfil)
│   ├── cliente-propostas-recebidas.component.ts (/cliente/solicitacao/:id/propostas)
│   ├── cookie-banner.component.ts
│   ├── footer/footer.component.ts
│   ├── fotos-prestador/fotos-prestador.component.ts (grid de portfólio no perfil)
│   ├── global-snack-bar/global-snack-bar.component.ts
│   ├── home/home.component.ts
│   ├── login.component.ts
│   ├── navbar.component.ts                    (sino com polling 30s e badge de notificações)
│   ├── notification/notification.component.ts
│   ├── notification-container/notification-container.component.ts
│   ├── onboarding-checklist/onboarding-checklist.component.ts (checklist 7 critérios + barra de progresso)
│   ├── perfil-publico/perfil-publico.component.ts (/prestador/:id — sem autenticação)
│   ├── politica-privacidade.component.ts
│   ├── prestador-avaliacao.component.ts       (/prestador/solicitacao/:id/avaliacao)
│   ├── prestador-dashboard.component.ts       (oportunidades no topo + copiar link de perfil)
│   ├── prestador-enviar-proposta.component.ts (/prestador/solicitacao/:id)
│   ├── prestador-perfil/prestador-perfil.component.ts (avatar upload, telefone, forkJoin para salvar)
│   ├── register/register.component.ts
│   ├── seguranca.component.ts
│   ├── seja-profissional.component.ts
│   ├── seletor-uf.component.ts
│   ├── sobre-nos.component.ts
│   ├── termos-uso.component.ts
│   └── verify-account.component.ts
├── directives/
│   ├── mask.directive.ts
│   └── uppercase.directive.ts
├── guards/
│   └── auth.guard.ts                          (authGuard, clienteGuard, prestadorGuard, adminGuard)
├── interceptors/
│   └── auth.interceptor.ts                    (JWT no header + trata 401 → refresh → /login)
├── models/
│   └── models.ts                              (inclui MensagemChat, FotoPrestador, PerfilPublico, CompletudePrestador)
├── services/
│   ├── auth.service.ts
│   ├── avaliacao.service.ts
│   ├── categoria.service.ts
│   ├── chat.service.ts
│   ├── foto.service.ts                        (uploadAvatar, deletarAvatar, upload portfólio, listar fotos)
│   ├── notification.service.ts
│   ├── prestador.service.ts                   (inclui getCompletude, getPerfilPublico, buscarProximos)
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
| latitude | Double | nullable (V3) |
| longitude | Double | nullable (V3) |
| fotoPerfilUrl | String | TEXT, nullable (V9) |
| percentualCompletude | Integer | NOT NULL, default 0 (V9) — recalculado a cada save |
| verificado | Boolean | NOT NULL, default false (V12) — recalculado a cada avaliação e a cada conclusão de serviço |

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
| dataCriacao | LocalDateTime | NOT NULL, auto (`@CreationTimestamp`) |

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
| GET | `/api/prestadores/{id}/publico` | Pública | Perfil público — sem dados sensíveis | `PerfilPublicoDTO`; 404 se inativo |
| GET | `/api/prestadores/me` | JWT | Perfil do prestador logado | — |
| GET | `/api/prestadores/me/completude` | JWT (`ROLE_PRESTADOR`) | Retorna checklist de completude | `CompletudePrestadorDTO` com 7 itens ponderados |
| GET | `/api/prestadores/proximo` | Pública | Busca por raio com Haversine | Params: `lat`, `lng`, `raioKm`, `categoriaId` |
| POST | `/api/prestadores` | JWT (`ROLE_PRESTADOR`) | Cria ou atualiza perfil de prestador | ✅ Exige `ROLE_PRESTADOR` |
| PUT | `/api/prestadores/{id}` | JWT | Atualiza perfil | — |
| POST | `/api/prestadores/me/foto-perfil` | JWT (`ROLE_PRESTADOR`) | Upload de avatar (salvo em `/uploads/prestadores/{id}/avatar.*`) | Recalcula completude |
| DELETE | `/api/prestadores/me/foto-perfil` | JWT (`ROLE_PRESTADOR`) | Remove avatar | Recalcula completude |
| POST | `/api/prestadores/me/fotos` | JWT (`ROLE_PRESTADOR`) | Upload de foto de portfólio (máx 10) | Armazenamento em volume Docker |
| DELETE | `/api/prestadores/me/fotos/{fotoId}` | JWT (`ROLE_PRESTADOR`) | Remove foto de portfólio | — |
| GET | `/api/prestadores/{id}/fotos` | Pública | Lista fotos de portfólio | — |

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
| POST | `/api/propostas` | JWT | Envia proposta | ✅ `PropostaController` desacoplado — `PrestadorRepository` removido; resolução movida para `PropostaService` |
| GET | `/api/propostas/solicitacao/{id}` | JWT | Lista propostas de uma solicitação | ⚠️ Qualquer autenticado acessa propostas de qualquer solicitação |
| GET | `/api/propostas/minhas` | JWT | Propostas do prestador logado | — |
| PATCH | `/api/propostas/{id}/aceitar` | JWT | Aceita proposta | ⚠️ Sem verificação de que o usuário é dono da solicitação |
| PATCH | `/api/propostas/{id}/recusar` | JWT | Recusa proposta | ⚠️ Mesmo problema |

### `/api/chat`
| Método | Path | Auth | O que faz | Observações |
|--------|------|------|-----------|-------------|
| GET | `/api/chat/proposta/{propostaId}` | JWT | Lista mensagens do chat de uma proposta | Somente leitura se proposta RECUSADA ou solicitação CONCLUIDA/CANCELADA |
| POST | `/api/chat/proposta/{propostaId}` | JWT | Envia mensagem no chat | — |

### `/api/notificacoes`
| Método | Path | Auth | O que faz |
|--------|------|------|-----------|
| GET | `/api/notificacoes` | JWT | Lista notificações do usuário logado |
| GET | `/api/notificacoes/nao-lidas` | JWT | Contagem de notificações não lidas |
| PATCH | `/api/notificacoes/{id}/lida` | JWT | Marca notificação como lida |
| PATCH | `/api/notificacoes/marcar-todas-lidas` | JWT | Marca todas como lidas |

### `/api/admin`
| Método | Path | Auth | O que faz |
|--------|------|------|-----------|
| GET | `/api/admin/stats` | JWT (`ROLE_ADMIN`) | Totais de usuários, prestadores, solicitações, propostas e usuários online |
| GET | `/api/admin/usuarios` | JWT (`ROLE_ADMIN`) | Lista todos os usuários paginada (inclui `online`, `ultimoAcesso`) |
| GET | `/api/admin/usuarios/{id}` | JWT (`ROLE_ADMIN`) | Busca usuário por ID |
| PATCH | `/api/admin/usuarios/{id}/ativar` | JWT (`ROLE_ADMIN`) | Ativa ou desativa usuário |
| GET | `/api/admin/usuarios/online` | JWT (`ROLE_ADMIN`) | Usuários ativos nos últimos 5 min (lista `UserResumoDTO`) |
| GET | `/api/admin/solicitacoes` | JWT (`ROLE_ADMIN`) | Lista todas as solicitações com filtro opcional `?status=` (paginado, `SolicitacaoAdminDTO`) |
| PATCH | `/api/admin/solicitacoes/{id}/status` | JWT (`ROLE_ADMIN`) | Altera status de solicitação (bypass de ownership) |
| GET | `/api/admin/email/config` | JWT (`ROLE_ADMIN`) | Retorna configuração Brevo atual (API key mascarada) |
| PUT | `/api/admin/email/config` | JWT (`ROLE_ADMIN`) | Salva configuração de e-mail (API key, remetente, toggle ativo) |
| POST | `/api/admin/email/testar` | JWT (`ROLE_ADMIN`) | Envia e-mail de teste para o endereço informado |

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
| POST | `/api/categorias` | JWT (`ROLE_ADMIN`) | Cria categoria | ✅ Exige `ROLE_ADMIN` via `SecurityFilterChain` + `@PreAuthorize` |
| PUT | `/api/categorias/{id}` | JWT (`ROLE_ADMIN`) | Atualiza | ✅ Mesmo controle |
| DELETE | `/api/categorias/{id}` | JWT (`ROLE_ADMIN`) | Deleta | ✅ Mesmo controle |

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
| `ClienteDashboardComponent` | `/cliente/dashboard` | clienteGuard | ✅ Funcional — lista solicitações, modal de detalhes, botão "Concluir", botão "Avaliar", botão "Editar" navega corretamente |
| `ClienteNovaSolicitacaoComponent` | `/cliente/nova-solicitacao` | clienteGuard | ✅ Funcional |
| `ClienteEditarSolicitacaoComponent` | `/cliente/editar-solicitacao/:id` | clienteGuard | ⚠️ Existente — verificar se totalmente implementado |
| `ClientePerfilComponent` | `/cliente/perfil` | clienteGuard | ✅ Funcional — edição de dados pessoais e endereço; aba "Minhas Avaliações" com média agregada |
| `ClientePropostasRecebidasComponent` | `/cliente/solicitacao/:id/propostas` | clienteGuard | ✅ Funcional — aceitar/recusar com confirmação |
| `ClienteAvaliacaoComponent` | `/cliente/solicitacao/:id/avaliacao` | clienteGuard | ✅ Funcional — estrelas 1–5, comentário, estados completos |
| `PrestadorDashboardComponent` | `/prestador/dashboard` | prestadorGuard | ✅ Funcional — oportunidades no topo, filtro cidade/estado, botão "Ver perfil público" + "Copiar link" |
| `PrestadorPerfilComponent` | `/prestador/perfil` | prestadorGuard | ✅ Funcional — avatar upload, telefone, forkJoin salva perfil+usuário em paralelo |
| `PrestadorEnviarPropostaComponent` | `/prestador/solicitacao/:id` | prestadorGuard | ✅ Funcional — detalhes da solicitação + formulário de proposta |
| `PrestadorAvaliacaoComponent` | `/prestador/solicitacao/:id/avaliacao` | prestadorGuard | ✅ Funcional — estrelas 1–5, comentário, estados completos |
| `OnboardingChecklistComponent` | (embed no dashboard) | prestadorGuard | ✅ Funcional — 7 critérios ponderados, barra colorida, collapse ao 100% |
| `PerfilPublicoComponent` | `/prestador/:id` | Pública | ✅ Funcional — avatar, badge Verificado, portfólio com modal, avaliações, CTA solicitar serviço |
| `ChatComponent` | `/chat/proposta/:id` | authGuard | ✅ Funcional — polling 3s, somente leitura após encerramento |
| `FotosPrestadorComponent` | (embed no perfil) | prestadorGuard | ✅ Funcional — grid com upload inline e remoção por hover |
| `BuscaPrestadoresComponent` | `/buscar-prestadores` | Pública | ✅ Funcional — geolocalização do navegador + Haversine no backend |
| `AdminDashboardComponent` | `/admin/dashboard` | adminGuard | ✅ Funcional — 5 cards de stats (inclui "Online agora"), 3 abas: Usuários (coluna online + ativação), Online (grid com dot animado), Solicitações (filtro + alteração inline de status) |

### Serviços Angular

| Service | Base URL | Status |
|---------|----------|--------|
| `AuthService` | `environment.apiUrl/auth` | ✅ Centralizado em `environment.ts` |
| `CategoriaService` | `environment.apiUrl/categorias` | ✅ Centralizado em `environment.ts` |
| `SolicitacaoService` | `environment.apiUrl/solicitacoes` | ✅ Centralizado em `environment.ts` |
| `PrestadorService` | `environment.apiUrl/prestadores` | ✅ Inclui getCompletude, getPerfilPublico, buscarProximos |
| `PropostaService` | `environment.apiUrl/propostas` | ✅ Centralizado em `environment.ts` |
| `AvaliacaoService` | `environment.apiUrl/avaliacoes` | ✅ Centralizado em `environment.ts` |
| `FotoService` | `environment.apiUrl/prestadores` | ✅ uploadAvatar, deletarAvatar, upload portfólio, listar fotos |
| `ChatService` | `environment.apiUrl/chat` | ✅ listar e enviar mensagens por propostaId |
| `AdminService` | `environment.apiUrl/admin` | ✅ stats, listarUsuarios, ativarUsuario, listarOnline, listarSolicitacoes, alterarStatusSolicitacao |
| `UserService` | `environment.apiUrl/users` | ✅ Centralizado em `environment.ts` |
| `ViaCEPService` | `https://viacep.com.br` | ✅ Busca endereço por CEP |
| `NotificationService` | — | ✅ Toasts e notificações em-tela |

---

## 6. DÉBITOS TÉCNICOS

### CRÍTICO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| C1 | `UserController` retorna entidade `User` diretamente (sem DTO) | `UserController.java` | ✅ **Resolvido** em 2026-05-12 — `UserController` removido; `GET /api/users/me` e `PUT /api/users/me` eram cobertos por `PerfilController` (usa `UpdatePerfilDTO`); `DELETE /api/users/me` coberto por `DELETE /api/auth/conta` |
| C2 | `PATCH /api/propostas/{id}/aceitar` e `/recusar` sem verificação de ownership | `PropostaController.java` | ✅ **Resolvido** em 2026-05-12 — verifica clienteId do dono da solicitação |
| C3 | `PATCH /api/solicitacoes/{id}/status` sem verificação de ownership | `SolicitacaoServicoController.java` | ✅ **Resolvido** em 2026-05-12 — verifica ownership antes de mudar status |
| C4 | `DataSeeder` sem profile guard — cria usuários com senha `123456` em todos os ambientes | `DataSeeder.java` | ✅ **Resolvido** em 2026-05-12 — `@Profile("!prod")` |
| C5 | Cadastro de usuário retornava 400 — `@Size`/`@Pattern` em `RegisterDTO.senha` validavam a string Base64 | `RegisterDTO.java` | ✅ **Resolvido** em 2026-05-12 |

### ALTO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| A1 | CORS hardcoded em `http://localhost:4200` | `SecurityConfig.java` + `PerfilController.java` | ✅ **Resolvido** em 2026-05-12 — lê `${app.frontend.url}` |
| A2 | URL do backend hardcoded `http://localhost:8080` em todos os services Angular | Todos `services/*.ts` | ✅ **Resolvido** em 2026-05-12 — centralizado em `environment.apiUrl` |
| A3 | Swagger exposto publicamente sem restrição | `SecurityConfig.java` | ✅ **Resolvido** em 2026-05-12 — `application-prod.yml` desabilita Swagger no perfil prod |
| A4 | Endpoints de categoria sem restrição de role ADMIN | `CategoriaController.java` | ✅ **Resolvido** em 2026-05-12 — `POST/PUT/DELETE /api/categorias` exigem `ROLE_ADMIN` via `SecurityFilterChain` + `@PreAuthorize`; `ADMIN` adicionado ao enum `TipoUsuario`; usuário admin no `DataSeeder` |
| A5 | `POST /api/prestadores` sem verificação de tipo — CLIENTE pode criar perfil de PRESTADOR | `PrestadorController.java` | ✅ **Resolvido** em 2026-05-12 — `POST /api/prestadores` exige `ROLE_PRESTADOR` via `SecurityFilterChain` + `@PreAuthorize` |
| A6 | `PUT /api/users/me` aceita entidade `User` no body — permite alterar `tipo`, `login` etc. | `UserController.java` | ✅ **Resolvido** em 2026-05-12 — `UserController` removido inteiramente; usar `PUT /api/perfil` com `UpdatePerfilDTO` |
| A7 | Sem interceptor de resposta 401 no Angular | — | ✅ **Resolvido** em 2026-05-12 — redireciona para /login ao receber 401 |
| A8 | Formulário de perfil do prestador nunca pré-prenchia os campos | `prestador-perfil.component.ts` | ✅ **Resolvido** em 2026-05-12 |

### MÉDIO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| M1 | `PropostaController` injeta `PrestadorRepository` diretamente | `PropostaController.java` | ✅ **Resolvido** em 2026-05-15 — `PrestadorRepository` removido do controller; `criar()` e `listarMinhas()` movidos para `PropostaService` |
| M2 | `POST /api/avaliacoes` sem validação de que o serviço foi concluído | `AvaliacaoService.java` | ✅ **Resolvido** — valida status CONCLUIDA |
| M3 | Sem proteção contra avaliação duplicada | `AvaliacaoService.java` | ✅ **Resolvido** — unicidade por (solicitacaoId, TipoAvaliacao) |
| M4 | JWT expira em 24h sem refresh token | `JwtTokenProvider.java` | ✅ **Resolvido** em 2026-05-15 — `RefreshToken` entity + `RefreshTokenService` com rotação; `POST /api/auth/refresh`; interceptor Angular re-tenta com novo token |
| M5 | `isAuthenticated()` não valida expiração do JWT | `auth.service.ts` | ✅ **Resolvido** em 2026-05-15 — decodifica payload JWT (`atob`), checa `exp * 1000 > Date.now()`; retorna `true` se JWT válido ou se há refresh token |
| M6 | `canActivate: [clienteGuard, prestadorGuard]` em `/meu-perfil` bloqueava prestadores | `app.routes.ts` | ✅ **Resolvido** — rota `/meu-perfil` substituída por `/cliente/perfil` e `/prestador/perfil` separadas |
| M7 | Contadores no dashboard do prestador hardcoded em `0` | `prestador-dashboard.component.ts` | ✅ **Resolvido** — stats reais |
| M8 | Botão "Ver Detalhes e Enviar Proposta" sem handler | `prestador-dashboard.component.ts` | ✅ **Resolvido** — navega para `/prestador/solicitacao/:id` |
| M9 | Botão "Editar" na modal do cliente apenas fecha o modal | `cliente-dashboard.component.ts` | ✅ **Resolvido** em 2026-05-15 — `fecharModal()` chamado antes de `router.navigate()` em `editarSolicitacao()` |
| M10 | `ddl-auto: update` sem controle de migração (sem Flyway/Liquibase) | `application.yml` | ✅ **Resolvido** em 2026-05-15 — Flyway adicionado; `ddl-auto: none`; `V1__initial_schema.sql` + `V2__add_refresh_tokens.sql`; `baseline-on-migrate: true` para DBs existentes |
| M11 | `Avaliacao` sem campo `dataCriacao` | `Avaliacao.java` | ✅ **Resolvido** em 2026-05-15/16 — campo `dataCriacao` com `@CreationTimestamp` adicionado; `V7__add_data_criacao_avaliacoes.sql` adiciona a coluna ao banco existente (criado pelo Hibernate DDL antes do Flyway) |

### BAIXO

| # | Problema | Arquivo | Status |
|---|----------|---------|--------|
| B1 | Sem rate limiting em `POST /api/auth/login` | `SecurityConfig.java` | ✅ **Resolvido** em 2026-05-15 — `LoginRateLimitFilter` bloqueia IPs com > 10 tentativas/min com HTTP 429 |
| B2 | `PropostaService` e `AvaliacaoService` declarados mas não usados | — | ✅ **Resolvido** — ambos são usados pelos novos componentes |
| B3 | `buscarServico()` e `selecionarCategoria()` na home apenas fazem `console.log` | `home.component.ts` | ✅ **Resolvido** em 2026-05-15 — navega para `/register`, `/cliente/nova-solicitacao` ou `/prestador/dashboard` conforme papel; `?categoriaId` pré-seleciona categoria |
| B4 | Sem paginação visível no frontend | Todos os dashboards | ✅ **Resolvido** em 2026-05-15 — controles anterior/próxima + indicador de página nos dashboards cliente e prestador |
| B5 | Erros de HTTP tratados apenas com `console.error()` sem feedback visual | Múltiplos componentes | ✅ **Resolvido** em 2026-05-15 — banners de erro exibidos ao usuário em todos os componentes afetados |
| B6 | Templates HTML inline em arquivos TypeScript | Todos os componentes originais | ✅ **Resolvido** em 2026-05-15 — 19 componentes migrados para `templateUrl` com arquivos `.html` separados |
| B7 | Paineis do cliente e prestador mais largos que o navbar em telas grandes | `cliente-dashboard`, `prestador-dashboard` e outros | ✅ **Resolvido** em 2026-05-12 — `max-w-6xl` adicionado em 5 componentes |
| B8 | Rodapés inconsistentes entre as páginas (estilos, logo ausente, links divergentes) | `home`, `register`, `seguranca`, `seja-profissional` | ✅ **Resolvido** em 2026-05-12 — padrão unificado: logo + links (Sobre nós, Termos de uso, Política de Privacidade) + copyright |

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

### Bugs e débitos corrigidos

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
| M9: botão "Editar" no modal do cliente fechava sem navegar | `fix/m9-m1-m11-correcoes-medio` | 2026-05-15 |
| M1: `PropostaController` injetava `PrestadorRepository` diretamente (violação de camadas) | `fix/m9-m1-m11-correcoes-medio` | 2026-05-15 |
| M11: entidade `Avaliacao` sem `dataCriacao` — impossível ordenar avaliações | `fix/m9-m1-m11-correcoes-medio` | 2026-05-15 |
| Erro 500 na tela de avaliação — `column "data_criacao" of relation "avaliacoes" does not exist`; tabela criada pelo Hibernate DDL antes do Flyway sem a coluna; `V7__add_data_criacao_avaliacoes.sql` corrige o schema | `develop` | 2026-05-16 |
| `@Transactional(readOnly = true)` ausente em métodos de leitura — múltiplas chamadas a repositórios sem transação compartilhada causavam falha de sessão Hibernate em `SolicitacaoServicoService`, `PropostaService` e `AvaliacaoService` | `develop` | 2026-05-16 |
| `FluxoPrincipalTest` falhava ao carregar contexto por `MAIL_HOST` não resolvido — corrigido com `@TestPropertySource` | `develop` | 2026-05-16 |
| B3: `buscarServico()` e `selecionarCategoria()` na home faziam apenas `console.log` | `feature/prioridade2-ux-melhorias` | 2026-05-15 |
| B4: dashboards sem controles de paginação apesar do backend paginar | `feature/prioridade2-ux-melhorias` | 2026-05-15 |
| B5: `console.error` sem feedback visual em múltiplos componentes | `feature/prioridade2-ux-melhorias` | 2026-05-15 |
| B1: `POST /api/auth/login` sem rate limiting (vulnerável a força bruta) | `feature/prioridade2-ux-melhorias` | 2026-05-15 |
| "Perfil não encontrado" ao acessar `/prestador/:id` — container backend executava JAR anterior a P4.4; o endpoint `GET /api/prestadores/{id}/publico` não existia no binário; `PerfilPublicoComponent` interpreta qualquer erro como 404; corrigido com `docker-compose up --build -d backend` (rebuild do JAR) | `main` | 2026-05-18 |
| fix/cobertura-de-testes: 117 testes unitários backend passando (PrestadorService, FotoPrestadorService, PropostaService, AvaliacaoService, NotificacaoService); 30 testes de segurança backend (PrestadorSegurancaTest, ChatSegurancaTest, AdminSegurancaTest); 69/70 testes frontend (ChatComponent, OnboardingChecklist, PerfilPublico, PrestadorDashboard); gap corrigido — `GET /api/prestadores/me/**` agora exige `ROLE_PRESTADOR` em `SecurityConfig`; `@WithMockUser` substituido por `.with(user(buildUser(...)))` com entidade User customizada; serializacao de `PageImpl` corrigida com `PageRequest.of(0,10)` | `fix/cobertura-de-testes` | 2026-05-18 |
| seed_dados.py: script Python para popular o banco com dados realistas — 10 clientes, 10 prestadores (com perfil, categorias e coordenadas), 15 fluxos completos (solicitacao, proposta, chat, aceitar, concluir, avaliacoes mutuas), 60 mensagens de chat, 30 avaliacoes com notas variadas (2 a 5 estrelas); gerencia rate limit de login (10/min) via pausas de 65s entre janelas | raiz do projeto | 2026-05-18 |

> **Nota operacional:** o frontend e servido via volume Docker com hot-reload (`ng serve --poll 1000`). O backend e um JAR compilado — qualquer mudanca em codigo Java exige `docker-compose up --build -d backend` para recompilar o JAR e reiniciar o container. Sem o rebuild, novos endpoints retornam 404.

---

## 8. O QUE FALTA IMPLEMENTAR

### Análise Competitiva — GetNinjas e Cronoshare (2026-05-15)

Levantamento realizado comparando o VemResolver com [GetNinjas](https://www.getninjas.com.br/) e [Cronoshare](https://www.cronoshare.com.br/).

#### Gap Analysis

| Funcionalidade | GetNinjas | Cronoshare | VemResolver |
|---|:---:|:---:|:---:|
| Chat interno (cliente ↔ prestador) | ✅ | ✅ | ✅ |
| Portfólio de fotos de trabalhos realizados | ✅ | ✅ | ❌ |
| Perfil público e URL compartilhável | ✅ | ✅ | ❌ |
| Onboarding guiado com progress bar de perfil | ✅ | ✅ | ❌ |
| Notificações por e-mail transacionais | ✅ | ✅ | ❌ |
| Badge de destaque / profissional verificado | — | ✅ (Top 5%) | ✅ (≥5 contratos + média ≥4,0) |
| Avaliação multidimensional | — | ✅ (0–10 + dimensões) | ❌ (só 1–5) |
| Verificação de identidade (documento + selfie) | ✅ (IdWall) | ✅ (manual) | ❌ |
| Mais de 100 categorias de serviço | ✅ (500+) | ✅ (200+) | ❌ (19) |

#### Diferenciais identificados

**GetNinjas:**
- Verificação de identidade com IA (IdWall) — 21% dos cadastros reprovados, comunicado como dado de confiança
- Link de avaliações compartilhável fora da plataforma
- Academia Ninja: vídeos de capacitação gratuitos para prestadores
- Modelo pay-per-lead: prestador compra "moedas" para desbloquear dados do cliente
- Selos de confiança na home (RA1000 Reclame Aqui + IdWall)

**Cronoshare:**
- Acreditações e certificações verificadas por humanos (CREA, CRO, OAB) com badge no perfil
- Badge "Profissional Top" para os melhores 5% por categoria
- Créditos sem expiração (diferencial sobre GetNinjas)
- Garantia de reembolso parcial de crédito se lead não converter
- Revisão humana de cada solicitação antes de distribuir — filtra spam

### Prioridade 4 — Competitividade (backlog)

| # | Funcionalidade | Justificativa | Impacto | Esforço |
|---|---|---|---|---|
| P4.1 | **Chat interno** — um chat por proposta, disponível antes e após o aceite | Permite negociação antes do aceite; mantém histórico; abre caminho para monetização por contato | Alto | Médio | ✅ Entregue em 2026-05-15 |
| P4.2 | **Portfólio de fotos** no perfil do prestador (até 10 imagens) | Principal elemento de conversão em serviços visuais (pintura, reforma, cozinha) | Alto | Baixo | ✅ Entregue em 2026-05-17 |
| P4.3 | **Onboarding guiado** com barra de progresso e checklist do perfil | Reduz taxa de prestadores inativos por perfil incompleto | Médio-Alto | Baixo | ✅ Entregue em 2026-05-18 |
| P4.4 | **Perfil público compartilhável** em `/prestador/{id}` com CTA para solicitar | Prestador não tem URL pública para divulgar; gera tráfego orgânico | Médio-Alto | Baixo | ✅ Entregue em 2026-05-18 |
| P4.5 | **Notificações por e-mail** transacionais (nova proposta, proposta aceita, nova solicitação) | Polling 30s depende da aba aberta; e-mail garante alcance offline | Alto | Baixo-Médio | ✅ Entregue em 2026-05-19 |
| P4.6 | **Badge "Verificado"** (5+ contratos + média ≥ 4,0) | Nenhum diferencial visual entre prestadores; cria gamificação de reputação | Médio | Baixo | ✅ Entregue em 2026-05-19 |
| P4.7 | **Avaliação multidimensional** — pontualidade, qualidade, comunicação | Nota única é pouco granular; dimensões ajudam cliente a escolher e prestador a melhorar | Médio | Médio |

> Casos de uso detalhados (fluxo principal, fluxos alternativos, regras de negócio e critérios de aceite) em [`docs/CASOS-DE-USO-P4.md`](CASOS-DE-USO-P4.md).

---

## 9. ITENS CONCLUÍDOS

### Fluxo central do marketplace

| Feature | Branch | Data |
|---------|--------|------|
| Infraestrutura Docker corrigida (variáveis de ambiente) | — | 2026-05-11 |
| Tela de envio de proposta (prestador) | `feature/envio-proposta-prestador` | 2026-05-11 |
| Propostas recebidas + aceitar/recusar com confirmação (cliente) | `feature/propostas-recebidas-cliente` | 2026-05-11 |
| Marcar serviço como CONCLUÍDO — endpoint `/concluir` com ownership | `feature/marcar-servico-concluido` | 2026-05-12 |
| Avaliação do prestador pelo cliente — estrelas 1–5, comentário | `feature/tela-avaliacao` | 2026-05-12 |
| Avaliação bidirecional — prestador avalia cliente; notas nos perfis | `feature/avaliacao-bidirecional` | 2026-05-12 |
| Painel do prestador com contratos em andamento e estatísticas reais | `feature/prestador-contratos-andamento` | 2026-05-12 |

### Segurança e infraestrutura

| Item | Branch | Data |
|------|--------|------|
| CORS via variável de ambiente `${FRONTEND_URL}` | `fix/bug7-cors-bug8-interceptor-401` | 2026-05-12 |
| URLs da API Angular centralizadas em `environment.ts` | `fix/bug6-environment-urls` | 2026-05-12 |
| Interceptor Angular de 401 — redireciona para `/login` | `fix/bug7-cors-bug8-interceptor-401` | 2026-05-12 |
| Swagger desabilitado em produção (`application-prod.yml`) | `fix/seguranca-ownership-dataseed-swagger` | 2026-05-12 |
| `DataSeeder` restrito ao perfil `!prod` | `fix/seguranca-ownership-dataseed-swagger` | 2026-05-12 |
| Ownership em aceitar/recusar propostas e alterar status | `fix/seguranca-ownership-dataseed-swagger` | 2026-05-12 |
| Endpoints de categoria restritos a `ROLE_ADMIN` | `fix/seguranca-a4-a5-a6` | 2026-05-12 |
| Criação de perfil de prestador restrita a `ROLE_PRESTADOR` | `fix/seguranca-a4-a5-a6` | 2026-05-12 |
| `UserController` removido — substituído por `PerfilController` com DTOs seguros | `fix/seguranca-a4-a5-a6` | 2026-05-12 |
| Rodapé padronizado em todas as páginas públicas | `fix/footer-padronizacao` | 2026-05-12 |

### Bugs corrigidos

| Bug | Branch | Data |
|-----|--------|------|
| Banner "complete seu perfil" não desaparecia após salvar | `fix/perfil-prestador-banner` | 2026-05-11 |
| `PATCH` ausente no CORS — 403 em preflight de aceitar/recusar/concluir | `fix/cors-patch-method` | 2026-05-12 |
| Unicidade de avaliação e badges "Avaliado ✓" nos dashboards | `fix/avaliacao-regras-visibilidade` | 2026-05-12 |
| Formulário do prestador nunca pré-prenchia — `buscarPorUsuario` → `buscarMeuPerfil` | — | 2026-05-12 |
| Overflow horizontal nos painéis do cliente e do prestador | — | 2026-05-12 |
| Painel do cliente mais largo que o navbar em telas grandes | `fix/dashboard-max-width` | 2026-05-12 |
| Botão "Ver Propostas" exibido indevidamente em serviços concluídos/cancelados | — | 2026-05-12 |
| Cadastro de novos usuários retornava 400 (validação Base64 em `RegisterDTO.senha`) | `fix/bug5-cadastro-400-datas` | 2026-05-12 |
| Campo data de expedição com `type="date"` exigia formato `YYYY-MM-DD` | `fix/bug5-cadastro-400-datas` | 2026-05-12 |
| M9: botão "Editar" no modal do cliente fechava sem navegar | `fix/m9-m1-m11-correcoes-medio` | 2026-05-15 |
| M1: `PropostaController` injetava `PrestadorRepository` diretamente (violação de camadas) | `fix/m9-m1-m11-correcoes-medio` | 2026-05-15 |
| M11: entidade `Avaliacao` sem `dataCriacao` — impossível ordenar avaliações | `fix/m9-m1-m11-correcoes-medio` | 2026-05-15 |
| M4: JWT sem refresh token — usuário deslogado após 24h | `feature/prioridade1-jwt-refresh-flyway` | 2026-05-15 |
| M5: `isAuthenticated()` só verificava presença do token, não a expiração | `feature/prioridade1-jwt-refresh-flyway` | 2026-05-15 |
| M10: `ddl-auto: update` sem controle de versão do schema | `feature/prioridade1-jwt-refresh-flyway` | 2026-05-15 |

---

### Prioridade 4 — Competitividade (parcial)

| Item | Branch | Data |
|------|--------|------|
| P4.1: Chat por proposta — `MensagemChat` entity + `ChatService` + `ChatController` + `V5__add_mensagens_chat.sql` + `V6__chat_por_proposta.sql`; chat privado por proposta (pré e pós aceite); `ChatComponent` Angular com polling 3s, envio otimista; somente leitura se proposta RECUSADA ou solicitação CONCLUIDA/CANCELADA; botão "💬 Chat" integrado nos dashboards e na tela de propostas recebidas; 11 testes unitários `ChatServiceTest` (CT-01-01 a CT-01-11) | `develop` | 2026-05-15 |
| fix/chat-polling-3s: intervalo de polling do `ChatComponent` reduzido de 30s para 3s — elimina delay perceptível para o receptor sem necessidade de SSE, adequado ao volume atual do projeto | `fix/chat-polling-3s` | 2026-05-17 |
| feat/prestador-dashboard-redesign: `PrestadorDashboardComponent` redesenhado — oportunidades (solicitações abertas) movidas para o topo com filtro reativo por cidade/estado; cards convertidos para lista vertical consistente com o dashboard do cliente; contratos, propostas e concluídos abaixo; seção "Meu Perfil" removida (redundante com botão no header); `FormsModule` adicionado; tratamento de erro no carregamento | `feature/prestador-dashboard-redesign` | 2026-05-17 |
| feat/p4.2-portfolio-fotos: `FotoPrestador` entity + `FotoPrestadorService` (upload, delete, list) + `FotoPrestadorController` (POST/DELETE `/api/prestadores/me/fotos`, GET `/{id}/fotos`); `V8__add_fotos_prestador.sql`; armazenamento em disco via volume Docker `uploads_data`; `WebConfig` serve `/uploads/**` como static; `SecurityConfig` libera GET `/uploads/**` e restringe DELETE; `FotosPrestadorComponent` Angular com grid, upload inline e remoção por hover; integrado em `PrestadorPerfilComponent`; completude do perfil passa a 5 critérios incluindo fotos | `feature/p4.2-portfolio-fotos` | 2026-05-17 |
| fix/chat-p4-1: DataSeeder corrigido — cria perfil `Prestador` para `maria@prestador.com`, solicitação ABERTA e proposta ENVIADA de teste; check por propostas ENVIADA garante que o dado é recriado após cada ciclo de testes, sem precisar de `docker-compose down -v`; chat disponível imediatamente após `docker-compose up` | `fix/chat-p4-1` | 2026-05-16 |
| feat/p4.4-perfil-publico: página pública `/prestador/:id` sem autenticação — `PerfilPublicoComponent` com avatar, nome, badge Verificado, categorias, cidade+estado, estrelas, portfólio em grid clicável (modal de ampliação), últimas 5 avaliações com nota e comentário; botão "Solicitar Serviço" redireciona para `/register?tipo=CLIENTE` (visitante) ou `/cliente/nova-solicitacao` (cliente logado); botão "Copiar link do perfil" usa Clipboard API; botão "Ver perfil público" + "Copiar link" integrados no `PrestadorDashboardComponent`; `GET /api/prestadores/{id}/publico` → `PerfilPublicoDTO` (sem e-mail, telefone ou CPF); badge Verificado calculado dinamicamente (5+ contratos ACEITA+CONCLUIDA e média ≥ 4,0); 404 amigável para ID inválido ou prestador inativo | `feature/p4.4-perfil-publico` | 2026-05-18 |
| feat/p4.3-onboarding-checklist: checklist de completude do perfil do prestador com 7 critérios ponderados (foto de perfil 15%, descrição 20%, categorias 15%, telefone 10%, cidade+estado 10%, portfólio de fotos 20%, lat/lng 10%); barra de progresso com cor dinâmica (vermelho <50%, amarelo 50–79%, verde ≥80%); collapse automático ao 100%; avatar upload (`POST/DELETE /api/prestadores/me/foto-perfil`); campo de telefone editável na tela de perfil; `percentual_completude` persistido em `prestadores` e recalculado a cada save de perfil ou foto; `V9__add_foto_perfil_e_completude.sql`; `OnboardingChecklistComponent` integrado no `PrestadorDashboardComponent`; endpoint `GET /api/prestadores/me/completude` retorna `CompletudePrestadorDTO` com itens | `feature/p4.3-onboarding-checklist` | 2026-05-18 |
| fix: "Perfil não encontrado" ao acessar perfil público — container backend rodava JAR pré-P4.4; `GET /api/prestadores/{id}/publico` inexistente causava 404; corrigido via `docker-compose up --build -d backend` | `main` | 2026-05-18 |
| fix/cobertura-de-testes: testes backend (117 unitarios + 30 segurança) e frontend (69/70) corrigidos e expandidos; gap de segurança em `SecurityConfig` corrigido (`GET /api/prestadores/me/**` exige `ROLE_PRESTADOR`); tecnicas: `.with(user(buildUser(...)))` para autenticacao com User customizado, `PageRequest.of(0,10)` para serializar PageImpl corretamente | `fix/cobertura-de-testes` | 2026-05-18 |
| feature/p4.5-email-transacional: e-mail transacional via Brevo HTTP API (sem SMTP); `BrevoEmailService` usa `java.net.http.HttpClient`; `ConfiguracaoEmail` entity + repository + `EmailConfigService`; 3 endpoints admin (`GET/PUT /api/admin/email/config`, `POST /api/admin/email/testar`); API key mascarada na resposta; disparos em proposta recebida, aceita e contrato concluído; `V11__add_configuracoes_email.sql`; aba "✉️ E-mail" no admin dashboard com form, toggle ativo e botão de teste; 8 `BrevoEmailServiceTest` + 11 `AdminEmailSegurancaTest`; totais: 146 backend, 90/91 frontend | `feature/p4.5-email-transacional` | 2026-05-19 |
| seed_dados.py: script Python na raiz do projeto para popular o banco com dados de demonstracao — 10 clientes + 10 prestadores com perfil completo + 15 fluxos completos (solicitacao, proposta, 4 mensagens de chat por fluxo, aceitar, concluir, avaliacao mutua); notas variadas de 2 a 5 estrelas; pausas automaticas de 65s para respeitar o rate limit de 10 logins/min | raiz | 2026-05-18 |
| feature/admin-online-solicitacoes: painel admin expandido com 3 funcionalidades novas — (1) rastreamento de presença via `OnlineTrackingFilter` (throttle 2 min, não @Component, registrado via `addFilterAfter`); campo `ultimo_acesso` em `usuarios` via `V10__add_ultimo_acesso_usuario.sql`; (2) aba "Online agora" com grid de usuários ativos nos últimos 5 min; (3) aba "Solicitações" com listagem paginada + filtro por status + alteração inline de status pelo admin; `SolicitacaoAdminDTO` com `totalPropostas`; `UserResumoDTO` ampliado com `ultimoAcesso` e `online`; `AdminStats` com `usuariosOnline`; 9 novos testes de segurança backend (AdminSegurancaTest); 13 novos testes frontend (AdminDashboardComponent); `@MockBean UserRepository` adicionado a `CategoriaSecurityTest`, `ChatSegurancaTest` e `PrestadorSegurancaTest` para corrigir context load após SecurityConfig depender de UserRepository | `feature/admin-online-solicitacoes` | 2026-05-18 |

---

### Prioridade 3 — Evolução do produto

| Item | Branch | Data |
|------|--------|------|
| B6: migração de 19 templates HTML inline para arquivos `.html` separados | `feature/prioridade3-evolucao-produto` | 2026-05-15 |
| Sistema de notificações — `Notificacao` entity, `NotificacaoService`, `NotificacaoController`, triggers em PropostaService/AvaliacaoService/SolicitacaoService, `V3` migration | `feature/prioridade3-evolucao-produto` | 2026-05-15 |
| Painel admin — `AdminController` (`GET /api/admin/stats`, `GET /api/admin/usuarios`), `AdminDashboardComponent`, `adminGuard` | `feature/prioridade3-evolucao-produto` | 2026-05-15 |
| feature/admin-online-solicitacoes — online tracking, aba usuários online, aba solicitações com filtro e alteração de status | `feature/admin-online-solicitacoes` | 2026-05-18 |
| Busca geolocalizada — `GET /api/prestadores/proximo` com Haversine, `BuscaPrestadoresComponent`, coordenadas no perfil do prestador | `feature/prioridade3-evolucao-produto` | 2026-05-15 |
| Sino de notificações no navbar — polling 30 s, badge de contagem, dropdown "Marcar todas como lidas" | `feature/prioridade3-evolucao-produto` | 2026-05-15 |
| `AuthServiceTest` corrigido — `@Mock RefreshTokenService` e stub de `generateRefreshToken` adicionados | `feature/prioridade3-evolucao-produto` | 2026-05-15 |
| Login do admin falhava com "login/senha inválidos" — `CHECK CONSTRAINT usuarios_tipo_check` criado pelo Hibernate só permitia `CLIENTE` e `PRESTADOR`; `V4__add_admin_tipo_usuario.sql` recria o constraint incluindo `ADMIN` | `main` | 2026-05-15 |
| `DataSeeder` nunca criava o admin em bancos com dados existentes (`count() == 0` impedia a inserção); corrigido para verificar cada e-mail individualmente | `main` | 2026-05-15 |

---

*Atualizado em 2026-05-18 (feature/admin-online-solicitacoes) — sincronizado com README.md.*
