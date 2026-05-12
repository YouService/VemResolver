# Documento de Arquitetura de Software — VemResolver

> **Versão:** 1.0  
> **Data:** 2026-05-11  
> **Status:** Em vigor

---

## 1. Visão Geral

O VemResolver é uma plataforma de marketplace de serviços que conecta clientes a prestadores autônomos. A arquitetura foi concebida para suportar crescimento horizontal, separação clara de responsabilidades e evolução independente entre frontend e backend.

---

## 2. Decisões Arquiteturais

### 2.1 Estilo arquitetural — Layered Architecture com Separation of Concerns

O backend adota uma arquitetura em camadas explícitas. Cada camada conhece apenas a camada imediatamente abaixo dela:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (Controllers, DTOs de entrada/saída)  │
├─────────────────────────────────────────┤
│         Application Layer               │
│   (Services — orquestração de casos     │
│    de uso, regras de negócio)           │
├─────────────────────────────────────────┤
│         Domain Layer                    │
│   (Entities JPA, Enums, Exceptions      │
│    de domínio)                          │
├─────────────────────────────────────────┤
│         Infrastructure Layer            │
│   (Repositories JPA, Security,          │
│    Configurações externas)              │
└─────────────────────────────────────────┘
```

**Regras de comunicação entre camadas:**

- Controllers recebem e retornam apenas DTOs — nunca entidades JPA
- Services contêm toda a lógica de negócio e orquestram os repositórios
- Repositories são interfaces Spring Data JPA — sem lógica de negócio
- A camada de segurança (JWT filter) intercepta requisições antes dos controllers

### 2.2 Arquitetura do Frontend — Angular Standalone Components

O frontend Angular 19 adota o modelo de componentes standalone com lazy loading por módulo funcional. A estrutura elimina `NgModule` e organiza a aplicação em torno de funcionalidades de negócio, não de tipos de arquivo.

---

## 3. Estrutura de Pacotes Recomendada

### 3.1 Backend — Estrutura Alvo

A estrutura atual organiza os arquivos por tipo (todos os controllers juntos, todos os services juntos). A estrutura alvo reorganiza por domínio de negócio:

```
com.marketplace.servicos/
│
├── auth/                          # Domínio de autenticação
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── dto/
│   │   ├── LoginDTO.java
│   │   ├── RegisterDTO.java
│   │   └── AuthResponseDTO.java
│   └── security/
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       └── CustomUserDetailsService.java
│
├── usuario/                       # Domínio de usuário
│   ├── UserController.java
│   ├── UserService.java
│   ├── User.java                  # Entidade JPA
│   ├── TipoUsuario.java           # Enum
│   ├── UserRepository.java
│   └── dto/
│       ├── UserResponseDTO.java   # NUNCA expõe campo senha
│       └── UserUpdateDTO.java
│
├── prestador/                     # Domínio do prestador
│   ├── PrestadorController.java
│   ├── PrestadorService.java
│   ├── Prestador.java
│   ├── PrestadorRepository.java
│   └── dto/
│       └── PrestadorDTO.java
│
├── categoria/                     # Domínio de categorias
│   ├── CategoriaController.java
│   ├── CategoriaService.java
│   ├── Categoria.java
│   ├── CategoriaRepository.java
│   └── dto/
│       └── CategoriaDTO.java
│
├── solicitacao/                   # Domínio de solicitações
│   ├── SolicitacaoServicoController.java
│   ├── SolicitacaoServicoService.java
│   ├── SolicitacaoServico.java
│   ├── StatusSolicitacao.java
│   ├── SolicitacaoServicoRepository.java
│   └── dto/
│       └── SolicitacaoServicoDTO.java
│
├── proposta/                      # Domínio de propostas
│   ├── PropostaController.java
│   ├── PropostaService.java
│   ├── Proposta.java
│   ├── StatusProposta.java
│   ├── PropostaRepository.java
│   └── dto/
│       └── PropostaDTO.java
│
├── avaliacao/                     # Domínio de avaliações
│   ├── AvaliacaoController.java
│   ├── AvaliacaoService.java
│   ├── Avaliacao.java
│   ├── AvaliacaoRepository.java
│   └── dto/
│       └── AvaliacaoDTO.java
│
├── shared/                        # Componentes transversais
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ErrorResponse.java
│   └── config/
│       ├── SecurityConfig.java
│       ├── OpenApiConfig.java
│       ├── DataSeeder.java
│       └── WebConfig.java
│
└── MarketplaceServicosApplication.java
```

### 3.2 Frontend — Estrutura Alvo

```
src/app/
│
├── core/                          # Singleton — instanciado uma vez
│   ├── services/
│   │   └── auth.service.ts
│   ├── interceptors/
│   │   ├── auth.interceptor.ts    # Injeta Bearer token
│   │   └── error.interceptor.ts   # Trata 401, 403, 500
│   ├── guards/
│   │   └── auth.guard.ts
│   └── models/
│       └── models.ts
│
├── shared/                        # Componentes reutilizáveis
│   ├── components/
│   │   ├── navbar/
│   │   │   ├── navbar.component.ts
│   │   │   └── navbar.component.html
│   │   ├── loading-spinner/
│   │   └── empty-state/
│   └── pipes/
│       └── status-label.pipe.ts
│
├── features/                      # Módulos funcionais (lazy loaded)
│   ├── home/
│   │   ├── home.component.ts
│   │   └── home.component.html
│   ├── auth/
│   │   ├── login/
│   │   │   ├── login.component.ts
│   │   │   └── login.component.html
│   │   └── register/
│   ├── cliente/
│   │   ├── dashboard/
│   │   ├── nova-solicitacao/
│   │   └── propostas-recebidas/   # A implementar
│   └── prestador/
│       ├── dashboard/
│       ├── perfil/
│       └── enviar-proposta/       # A implementar
│
├── app.routes.ts
├── app.config.ts
└── app.component.ts
```

---

## 4. Padrões de Integração

### 4.1 Comunicação Frontend ↔ Backend

- Toda comunicação ocorre via HTTP/JSON sobre REST
- O token JWT é transmitido no header `Authorization: Bearer <token>` em todas as requisições autenticadas, injetado automaticamente pelo `authInterceptor`
- O interceptor de erro (`errorInterceptor`) captura respostas 401 e redireciona ao login; captura 500 e exibe mensagem genérica ao usuário
- URLs base do backend são configuradas exclusivamente via `environment.ts`, nunca hardcoded nos services

### 4.2 Padrão de Resposta da API

Todas as respostas de erro seguem o contrato:

```json
{
  "timestamp": "2026-05-11T22:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Email já cadastrado",
  "errors": null,
  "path": "/api/auth/register"
}
```

Respostas de listagem paginada seguem o padrão Spring Data `Page<T>`:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 10,
  "number": 0
}
```

### 4.3 Segurança

- Autenticação: JWT stateless com algoritmo HMAC-SHA384 (JJWT 0.12.3)
- Tempo de expiração: 24 horas (configurável via variável de ambiente `JWT_EXPIRATION`)
- O secret JWT deve ser fornecido como string Base64 via variável de ambiente `JWT_SECRET` (não armazenar em código-fonte)
- CSRF desabilitado — sessões stateless dispensam proteção CSRF
- CORS configurado para aceitar apenas origens definidas via variável de ambiente

---

## 5. Modelo de Dados

### 5.1 Diagrama de entidades

```
┌──────────┐         ┌────────────────────┐
│  User    │─────────│  Prestador         │
│ (CLIENTE │1       1│  - descricaoPerfil │
│  ou      │         │  - cidade          │
│ PRESTADOR│         │  - estado          │
│ ou ADMIN)│         │  - avaliacaoMedia  │
└────┬─────┘         └────────┬───────────┘
     │                        │
     │1                       │N
     │                   ┌────┴──────────┐
     │N              ┌───│   Proposta    │
     │               │   │ - valor       │
┌────┴──────────┐    │   │ - mensagem   │
│ Solicitacao   │────┘   │ - status     │
│ Servico       │N  1    └───────────────┘
│ - descricao   │
│ - cidade      │─────────┐
│ - estado      │1        │1
│ - status      │   ┌─────┴──────┐
└───────────────┘   │ Avaliacao  │
                    │ - nota     │
                    │ - comentario│
                    └────────────┘

┌───────────┐       ┌────────────────┐
│ Categoria │N─────N│   Prestador    │
└───────────┘       └────────────────┘
              (via prestador_categorias)
```

### 5.2 Transições de estado

**SolicitacaoServico:**
```
ABERTA ──► EM_ANDAMENTO ──► CONCLUIDA
   │
   └──► CANCELADA
```

**Proposta:**
```
ENVIADA ──► ACEITA
   │
   └──► RECUSADA
```

---

## 6. Infraestrutura e Deploy

### 6.1 Ambiente local (Docker Compose)

O `docker-compose.yml` na raiz do projeto sobe três serviços:

- **postgres** — PostgreSQL 16 Alpine, porta `5432`
- **backend** — Spring Boot compilado na imagem, porta `8080`
- **frontend** — Node 22 Alpine servindo Angular CLI Dev Server, porta `4200`

### 6.2 Variáveis de ambiente obrigatórias (backend)

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `DB_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://postgres:5432/marketplace_db` |
| `DB_USERNAME` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `postgres` |
| `JWT_SECRET` | Secret JWT em Base64 (mín. 256 bits) | `bWFya2V0cGxhY2UtZGV2...` |
| `JWT_EXPIRATION` | Expiração do token em ms | `86400000` |

### 6.3 Variáveis de ambiente (frontend)

| Arquivo | Variável | Descrição |
|---------|----------|-----------|
| `environment.ts` | `apiUrl` | URL base da API para desenvolvimento |
| `environment.prod.ts` | `apiUrl` | URL base da API para produção |

---

## 7. Decisões em Aberto

- **Migração de schema:** substituir `ddl-auto: update` por Flyway para rastreabilidade de migrações
- **Controle de acesso granular:** implementar `TipoUsuario.ADMIN` e proteger endpoints administrativos com `@PreAuthorize("hasRole('ADMIN')")`
- **Refresh token:** implementar par access/refresh token para experiência sem interrupções
- **Notificações:** definir entre polling simples (GET periódico) ou WebSocket para notificações em tempo real
