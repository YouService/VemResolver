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

---

## O que fazer a seguir

Ordem sugerida por impacto no fluxo central do marketplace:

### 1. Marcar serviço como CONCLUÍDO *(alta prioridade)*
O endpoint já existe: `PATCH /api/solicitacoes/:id/status?status=CONCLUIDA`.
Falta um botão no painel do cliente para solicitações com status `EM_ANDAMENTO`.
Sugestão: adicionar botão "Marcar como Concluído" na tela `/cliente/solicitacao/:id/propostas` quando a solicitação está em andamento.

### 2. Tela de avaliação do prestador *(alta prioridade)*
Após a solicitação ir para `CONCLUIDA`, o cliente deve poder avaliar (nota 1–5 + comentário).
A API está completa: `POST /api/avaliacoes`.
Nenhum componente frontend existe. Criar em `/cliente/avaliar/:solicitacaoId`.

### 3. Bug: formulário de edição do perfil do prestador nunca pré-preenche
`prestador-perfil.component.ts:237` ainda chama `buscarPorUsuario(userId)` → endpoint inexistente.
Corrigir para `buscarMeuPerfil()` (mesmo fix já feito no dashboard).
**Verificar se o develop já tem isso corrigido antes de trabalhar.**

### 4. Bug: cadastro de novos usuários retorna 400
`RegisterDTO` no backend tem `@NotBlank` em CPF, RG e data de expedição, mas o frontend não envia esses campos.
Decidir: remover as anotações `@NotBlank` do backend (mais simples) ou adicionar os campos no formulário de cadastro.
**Verificar se o develop já tem isso corrigido antes de trabalhar.**

### 5. URLs da API hardcoded nos services Angular
Todos os services usam `http://localhost:8080` diretamente. Centralizar em `src/environments/environment.ts` antes de qualquer deploy em outro ambiente.

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
