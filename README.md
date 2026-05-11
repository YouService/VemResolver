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

## Pré-requisitos para desenvolvimento local

Você precisa de **uma** das duas opções abaixo:

**Opção A — Docker (recomendada):** apenas Docker Desktop instalado e em execução.

**Opção B — Manual:** Java 21 LTS, Maven 3.9+, Node.js 22 LTS, Angular CLI 19, PostgreSQL 16.

---

## Subindo o ambiente com Docker

Esta é a forma mais rápida. Sobe o banco, o backend e o frontend juntos.

**1. Clone o repositório:**

```bash
git clone https://github.com/YouService/youservice.git
cd youservice
```

**2. Suba os containers:**

```bash
docker-compose up
```

Na primeira execução, o Docker irá baixar as imagens e compilar o backend. O processo leva entre 3 e 10 minutos dependendo da conexão.

**3. Aguarde os logs de inicialização:**

O backend está pronto quando você ver:

```
marketplace-backend | Started MarketplaceServicosApplication in XX seconds
```

O frontend está pronto quando você ver:

```
marketplace-frontend | Application bundle generation complete.
marketplace-frontend |   ➜  Local: http://localhost:4200/
```

**4. Acesse a aplicação:**

- Frontend: `http://localhost:4200`
- API Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

**Para parar os containers:**

```bash
docker-compose down
```

**Para parar e apagar os dados do banco:**

```bash
docker-compose down -v
```

---

## Subindo manualmente (sem Docker)

### Backend

**1. Configure o banco PostgreSQL:**

Crie um banco de dados chamado `marketplace_db`:

```sql
CREATE DATABASE marketplace_db;
```

**2. Configure as variáveis de ambiente:**

Crie o arquivo `backend/.env.dev` com o conteúdo abaixo, substituindo os valores conforme seu ambiente:

```env
DB_URL=jdbc:postgresql://localhost:5432/marketplace_db
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_aqui
JWT_SECRET=bWFya2V0cGxhY2UtZGV2LXNlY3JldC1rZXktbWluaW11bS0yNTZiaXRzLWxlbmd0aC1vaw==
JWT_EXPIRATION=86400000
```

> O `JWT_SECRET` deve ser uma string codificada em Base64 com no mínimo 256 bits. O valor acima é adequado apenas para desenvolvimento local.

**3. Execute o backend:**

```bash
cd backend
mvn spring-boot:run
```

O servidor sobe na porta `8080`.

### Frontend

**1. Instale as dependências:**

```bash
cd frontend/marketplace-frontend
npm install
```

**2. Configure a URL da API:**

Edite `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

**3. Execute o servidor de desenvolvimento:**

```bash
npm start
```

O frontend sobe na porta `4200` com hot-reload ativo.

---

## Usuários de teste

O `DataSeeder` cria automaticamente dois usuários na primeira inicialização:

| Papel | Email | Senha |
|-------|-------|-------|
| Cliente | `joao@cliente.com` | `123456` |
| Prestador | `maria@prestador.com` | `123456` |

> Esses usuários existem apenas para fins de desenvolvimento. Em produção, o `DataSeeder` deve ser desabilitado via Spring Profile.

---

## Variáveis de ambiente

### Backend (obrigatórias)

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `DB_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/marketplace_db` |
| `DB_USERNAME` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `postgres` |
| `JWT_SECRET` | Secret JWT em Base64 (mín. 256 bits) | ver exemplo acima |
| `JWT_EXPIRATION` | Tempo de expiração do token em milissegundos | `86400000` (24h) |

### Frontend

As URLs e configurações de ambiente são centralizadas em:

- `src/environments/environment.ts` — desenvolvimento
- `src/environments/environment.prod.ts` — produção

Nunca altere URLs diretamente nos services Angular.

---

## Estrutura do repositório

```
vemresolver/
├── backend/                    # Spring Boot
│   ├── src/main/java/
│   │   └── com/marketplace/servicos/
│   ├── src/main/resources/
│   │   └── application.yml
│   └── Dockerfile
├── frontend/
│   └── marketplace-frontend/   # Angular 19
│       ├── src/app/
│       └── package.json
├── docs/                       # Documentação técnica
│   ├── DAS.md                  # Documento de Arquitetura
│   ├── REGRAS-DE-NEGOCIO.md   # Regras de negócio
│   ├── API.md                  # Documentação de API
│   └── LEVANTAMENTO.md         # Levantamento técnico completo
├── docker-compose.yml
└── README.md
```

---

## Executando os testes

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend/marketplace-frontend
npm test
```

---

## Documentação técnica adicional

Todos os documentos técnicos estão na pasta `docs/`:

- [`docs/DAS.md`](docs/DAS.md) — Documento de Arquitetura de Software
- [`docs/REGRAS-DE-NEGOCIO.md`](docs/REGRAS-DE-NEGOCIO.md) — Especificação completa das regras de negócio
- [`docs/API.md`](docs/API.md) — Documentação de API com exemplos e YAML OpenAPI
- [`docs/LEVANTAMENTO.md`](docs/LEVANTAMENTO.md) — Levantamento técnico completo: estado atual, débitos e plano de ação

---

## Contribuindo

- Crie uma branch a partir de `develop` seguindo o padrão: `feature/nome-da-feature`, `fix/nome-do-bug` ou `docs/nome-do-documento`
- Faça merge de volta para `develop` e, após validação, para `main`
- Compile e teste localmente antes de abrir pull request

---

## Autor

Felipe Andrade Lima — [GitHub](https://github.com/felipealima1984)
