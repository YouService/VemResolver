# Levantamento de Estado do Sistema — YouService

> **Última atualização:** 2026-04-29
> **Responsável pela análise:** Claude Sonnet 4.6

---

## ETAPA 1 — IDENTIDADE DO SISTEMA

**Nome:** YouService Marketplace
**Propósito:** Marketplace de serviços que conecta clientes (quem contrata) com prestadores de serviço (quem executa). O fluxo central é: cliente abre solicitação → prestadores enviam propostas → cliente aceita uma proposta → serviço é executado → cliente avalia.

**Público-alvo:** Pessoas físicas buscando serviços domésticos/profissionais (eletricistas, encanadores, pintores, diaristas, etc.) e os profissionais autônomos que os prestam.

**Stack tecnológica:**

| Camada | Tecnologia | Versão |
|---|---|---|
| Backend | Spring Boot | 3.2.1 |
| Linguagem backend | Java | 21 |
| ORM | Spring Data JPA / Hibernate | — |
| Segurança | Spring Security + JJWT | 0.12.3 |
| Banco de dados | PostgreSQL | 16 |
| Documentação API | Springdoc OpenAPI | 2.3.0 |
| Frontend | Angular | 19.2 |
| Linguagem frontend | TypeScript | 5.7 |
| Estilização | Tailwind CSS | 3.4 |
| Reatividade | RxJS | 7.8 |
| Build backend | Maven | — |
| Build frontend | Angular CLI / npm | — |
| Infraestrutura | Docker + Docker Compose | — |

**Repositório:** `d:\Felipe\dev\workspace\youservice` — branch única `main`.

---

## ETAPA 2 — ESTRUTURA DO PROJETO

**Organização de pastas:**

```
youservice/
├── backend/
│   ├── src/main/java/com/marketplace/servicos/
│   │   ├── config/        (SecurityConfig, WebConfig, OpenApiConfig, DataSeeder, DotenvConfig)
│   │   ├── controller/    (Auth, User, Categoria, SolicitacaoServico, Prestador, Proposta, Avaliacao)
│   │   ├── dto/           (RegisterDTO, LoginDTO, AuthResponseDTO, e demais DTOs de domínio)
│   │   ├── exception/     (GlobalExceptionHandler, BusinessException, ResourceNotFoundException)
│   │   ├── model/         (entidades JPA + enums de status)
│   │   ├── repository/    (interfaces JPA Repository)
│   │   ├── security/      (JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService)
│   │   └── service/       (Auth, User, Categoria, SolicitacaoServico, Prestador, Proposta, Avaliacao)
│   ├── env/               (.env.dev, .env.prod)
│   └── Dockerfile
└── frontend/marketplace-frontend/
    └── src/app/
        ├── components/    (Home, Login, Register, Navbar, e dashboards de cliente/prestador)
        ├── guards/        (authGuard, clienteGuard, prestadorGuard)
        ├── interceptors/  (auth.interceptor.ts)
        ├── models/        (models.ts — interfaces e enums)
        └── services/      (auth, user, categoria, solicitacao, prestador, proposta, avaliacao)
```

**Padrão arquitetural adotado:** Layered Architecture (Controller → Service → Repository) no backend; Standalone Components no Angular 19 no frontend.

**Desvios e inconsistências encontrados:**
- Templates HTML embutidos diretamente nos arquivos `.component.ts` (inline templates). Prejudica legibilidade em componentes grandes como `cliente-dashboard.component.ts` (409 linhas).
- Não há separação entre módulos de domínio no backend (pacotes planos por tipo: `controller/`, `service/`, etc.).
- `ddl-auto: update` no `application.yml` sem indicação de que muda para `validate` em produção.

---

## ETAPA 3 — FUNCIONALIDADES IMPLEMENTADAS

| Funcionalidade | Localização no código | Status | Observações |
|---|---|---|---|
| Cadastro de usuário | `AuthController`, `AuthService`, `register.component.ts` | ✅ Completa | Login gerado automaticamente a partir do email |
| Login / autenticação JWT | `AuthController`, `JwtTokenProvider`, `login.component.ts` | ✅ Completa | Token com 24h de validade; sem refresh token |
| Guard de rotas por papel | `auth.guard.ts` | ✅ Completa | authGuard, clienteGuard, prestadorGuard implementados |
| Interceptor de token HTTP | `auth.interceptor.ts` | ✅ Completa | Injeta header `Authorization` em todas as requests |
| Listagem de categorias | `CategoriaController`, `CategoriaService`, `categoria.service.ts` | ✅ Completa | Paginada, pública; 10 categorias pré-seeded |
| Criação de solicitação de serviço | `SolicitacaoServicoController`, `cliente-nova-solicitacao.component.ts` | ✅ Completa | Apenas para usuários CLIENTE |
| Listagem das próprias solicitações (cliente) | `ClienteDashboardComponent`, `/api/solicitacoes/minhas` | ✅ Completa | Filtro por status funcional |
| Cancelamento de solicitação | `ClienteDashboardComponent`, `DELETE /api/solicitacoes/{id}` | ✅ Completa | — |
| Visualização de detalhes de solicitação (cliente) | Modal em `ClienteDashboardComponent` | ⚠️ Parcial | Exibe dados; botão "Editar" chama `editarSolicitacao()` que apenas fecha o modal |
| Perfil do prestador (criar/editar) | `PrestadorPerfilComponent`, `PrestadorController` | ✅ Completa | Formulário com categorias, descrição e localização |
| Listagem de solicitações abertas (prestador) | `PrestadorDashboardComponent`, `/api/solicitacoes/abertas` | ✅ Completa | Exibe cards das solicitações disponíveis |
| Envio de proposta | `PropostaController`, `PropostaService` | ⚠️ Parcial | API completa; botão "Ver Detalhes e Enviar Proposta" no frontend não tem handler |
| Visualização das próprias propostas (prestador) | `/api/propostas/minhas` | ⚠️ Parcial | Endpoint existe; nenhum componente frontend consome |
| Aceitar/rejeitar proposta | `PropostaController`, `PropostaService` | ⚠️ Parcial | API completa; sem tela no frontend para o cliente |
| Avaliação de serviço | `AvaliacaoController`, `AvaliacaoService`, `avaliacao.service.ts` | ⚠️ Parcial | API completa; nenhum componente frontend implementado |
| Contadores do painel do prestador | `PrestadorDashboardComponent` | ❌ Quebrada | "Propostas Enviadas" e "Serviços Concluídos" exibem `0` hardcoded |
| Perfil do próprio usuário | `UserController`, `user-profile.component.ts` | ✅ Completa | GET e PUT /api/users/me implementados |
| Busca de prestadores | `PrestadorController`, `PrestadorService` | ✅ Completa | Filtro por categoria, cidade, estado e nota mínima |
| Tratamento global de erros (backend) | `GlobalExceptionHandler` | ✅ Completa | Cobre Business, NotFound, Validation, Auth e genéricos |
| Data seeding (dados de teste) | `DataSeeder` | ✅ Completa | Cria 10 categorias e 2 usuários de teste na inicialização |
| Documentação Swagger | `OpenApiConfig`, `/swagger-ui.html` | ✅ Completa | Exposto publicamente sem restrição de acesso |
| Soft delete de usuário | `UserController`, campo `ativo` | ✅ Completa | Marca `ativo = false`, não remove fisicamente |

---

## ETAPA 4 — DÍVIDAS TÉCNICAS E PROBLEMAS

**Funcionalidades incompletas sem indicação explícita:**
- `editarSolicitacao()` em `cliente-dashboard.component.ts:376` chama `fecharModal()` e para — sem implementação real.
- Botão "Ver Detalhes e Enviar Proposta" em `prestador-dashboard.component.ts:172` não tem `(click)` handler.
- Contadores "Propostas Enviadas" e "Serviços Concluídos" são valores literais `0` na template do prestador.

**Schema de banco sem controle de migração:**
- `ddl-auto: update` — alterações de modelo são aplicadas automaticamente sem rastreabilidade. Sem Flyway nem Liquibase.

**CORS hardcoded:**
- `SecurityConfig.java:63` — `allowedOrigins` fixo em `http://localhost:4200`. Impede deploy sem recompilação.

**Sem refresh token:**
- JWT expira em 24h sem mecanismo de renovação. Usuário é deslogado sem aviso.

**Sem rate limiting:**
- `POST /api/auth/login` sem proteção contra força bruta.

**Ausência de testes:**
- Zero testes no backend.
- Um único teste trivial no frontend (`app.component.spec.ts`).

**DataSeeder com senha fraca:**
- Cria usuários de teste com senha `"123456"` sem profile-guard — executado também em produção.

**Logs de erro silenciosos no frontend:**
- Erros de HTTP tratados apenas com `console.error()` sem feedback visual ao usuário.

**Swagger exposto publicamente:**
- Rota `/swagger-ui/**` pública via `SecurityConfig.java:49`. Expõe todos os contratos da API em produção.

**URL da API hardcoded no frontend:**
- `http://localhost:8080` definido diretamente nos services Angular sem uso de `environment.ts`.

---

## ETAPA 5 — O QUE CLARAMENTE FALTA

1. **Tela de detalhe e envio de proposta (prestador):** Fluxo central bloqueado no frontend.
2. **Tela de propostas recebidas (cliente):** Cliente não consegue ver, aceitar ou rejeitar propostas.
3. **Tela de avaliação:** Nenhum componente frontend para avaliar serviço concluído.
4. **Tela de edição de solicitação:** Botão existe, ação não implementada.
5. **Notificações ou atualizações em tempo real:** Sistema completamente estático/pull.
6. **Role ADMIN e painel administrativo:** Qualquer usuário autenticado pode criar/editar/deletar categorias.
7. **Ação de marcar serviço como "CONCLUÍDO":** Endpoint existe, não exposto no frontend.
8. **Paginação no frontend:** Sem controles visíveis de paginação nas listagens.
9. **Feedback de token expirado:** Sem interceptor de resposta para redirecionar ao login após 401.
10. **Variáveis de ambiente no frontend:** URL base da API hardcoded nos services.

---

## ETAPA 6 — RESUMO EXECUTIVO

O YouService tem uma base técnica sólida: modelagem de dados correta, backend cobrindo todos os domínios do fluxo principal com API bem estruturada, segurança JWT e tratamento de erros padronizado. Porém, o sistema está **funcionalmente incompleto**: o fluxo central do marketplace — prestador envia proposta, cliente aceita, serviço é concluído, cliente avalia — está implementado apenas no backend. No frontend, o nó crítico está quebrado: o botão de envio de proposta não faz nada, e não existe nenhuma tela para o cliente ver ou aceitar propostas recebidas. O que existe de fato operacional hoje é o cadastro de usuários, a criação de solicitações pelo cliente e a visualização dessas solicitações pelo prestador — as etapas de entrada do funil, sem o fechamento. Para se tornar utilizável como produto, o sistema precisa de ao menos três telas novas (detalhe/proposta do prestador, propostas recebidas do cliente, avaliação) e a correção do fluxo de status de serviço.

---

## HISTÓRICO DE ATUALIZAÇÕES

| Data | Descrição da mudança |
|---|---|
| 2026-04-29 | Levantamento inicial do estado do sistema |
