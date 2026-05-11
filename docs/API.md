# Documentação de API — VemResolver

> **Versão:** 1.0  
> **Base URL:** `http://localhost:8080`  
> **Autenticação:** Bearer JWT no header `Authorization`  
> **Formato:** JSON  
> **Documentação interativa:** `http://localhost:8080/swagger-ui.html`

---

## Autenticação

Todos os endpoints marcados com `🔒 JWT` exigem o header:

```
Authorization: Bearer <token>
```

O token é obtido via `POST /api/auth/login` e expira em 24 horas.

---

## Endpoint 1 — Busca de Prestadores

### `GET /api/prestadores`

Retorna uma lista paginada de prestadores com suporte a filtros por categoria, localização e nota mínima. Endpoint público — não requer autenticação.

**Parâmetros de query:**

- `categoriaId` *(Long, opcional)* — filtra por ID de categoria de serviço
- `cidade` *(String, opcional)* — filtra por cidade (case sensitive)
- `estado` *(String, opcional)* — filtra por UF (ex: `SP`, `RJ`)
- `minAvaliacao` *(Double, opcional)* — nota mínima do prestador (0.0 a 5.0)
- `page` *(Integer, padrão: 0)* — número da página (base zero)
- `size` *(Integer, padrão: 10)* — quantidade de itens por página
- `sort` *(String, opcional)* — campo e direção de ordenação (ex: `avaliacaoMedia,desc`)

**Exemplo de requisição:**

```http
GET /api/prestadores?categoriaId=1&estado=SP&minAvaliacao=4.0&page=0&size=10
```

**Resposta de sucesso — `200 OK`:**

```json
{
  "content": [
    {
      "id": 12,
      "usuarioId": 5,
      "categoriasIds": [1, 3],
      "descricaoPerfil": "Eletricista com 10 anos de experiência em instalações residenciais e comerciais.",
      "cidade": "São Paulo",
      "estado": "SP",
      "avaliacaoMedia": 4.8,
      "totalAvaliacoes": 23
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

**Possíveis erros:**

- `500 Internal Server Error` — falha inesperada no servidor

**Especificação OpenAPI:**

```yaml
/api/prestadores:
  get:
    tags:
      - Prestadores
    summary: Buscar prestadores com filtros
    parameters:
      - name: categoriaId
        in: query
        required: false
        schema:
          type: integer
          format: int64
      - name: cidade
        in: query
        required: false
        schema:
          type: string
      - name: estado
        in: query
        required: false
        schema:
          type: string
          minLength: 2
          maxLength: 2
      - name: minAvaliacao
        in: query
        required: false
        schema:
          type: number
          format: double
          minimum: 0
          maximum: 5
      - name: page
        in: query
        required: false
        schema:
          type: integer
          default: 0
      - name: size
        in: query
        required: false
        schema:
          type: integer
          default: 10
    responses:
      '200':
        description: Lista paginada de prestadores
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PagePrestadorDTO'
```

---

## Endpoint 2 — Criação de Solicitação de Serviço

### `POST /api/solicitacoes`

🔒 **JWT** — Exclusivo para usuários com `tipo = CLIENTE`.

Cria uma nova solicitação de serviço. A solicitação nasce com `status = ABERTA` e fica visível para todos os prestadores da categoria informada.

**Cabeçalhos obrigatórios:**

```
Authorization: Bearer <token>
Content-Type: application/json
```

**Corpo da requisição:**

```json
{
  "categoriaId": 1,
  "descricao": "Preciso trocar o quadro de distribuição elétrica de um apartamento de 70m². O quadro atual está sobrecarregado e apresenta oscilações de tensão.",
  "cidade": "São Paulo",
  "estado": "SP"
}
```

**Campos do body:**

- `categoriaId` *(Long, obrigatório)* — ID de uma categoria existente
- `descricao` *(String, obrigatório)* — descrição detalhada do serviço necessário
- `cidade` *(String, obrigatório)* — cidade onde o serviço será prestado
- `estado` *(String, obrigatório)* — UF (2 letras maiúsculas)

**Resposta de sucesso — `201 Created`:**

```json
{
  "id": 47,
  "clienteId": 3,
  "clienteNome": "João Silva",
  "categoriaId": 1,
  "categoriaNome": "Eletricista",
  "descricao": "Preciso trocar o quadro de distribuição elétrica de um apartamento de 70m²...",
  "cidade": "São Paulo",
  "estado": "SP",
  "status": "ABERTA",
  "dataCriacao": "2026-05-11T22:00:00"
}
```

**Possíveis erros:**

- `400 Bad Request` — campos obrigatórios ausentes ou categoria inexistente
- `401 Unauthorized` — token ausente ou inválido
- `403 Forbidden` — usuário com `tipo = PRESTADOR` tentando criar solicitação

**Especificação OpenAPI:**

```yaml
/api/solicitacoes:
  post:
    tags:
      - Solicitações de Serviço
    summary: Criar nova solicitação de serviço
    security:
      - bearer-jwt: []
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/SolicitacaoServicoDTO'
    responses:
      '201':
        description: Solicitação criada com sucesso
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SolicitacaoServicoDTO'
      '400':
        description: Dados inválidos
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Não autenticado
      '403':
        description: Usuário não é um cliente

components:
  schemas:
    SolicitacaoServicoDTO:
      type: object
      required:
        - categoriaId
        - descricao
        - cidade
        - estado
      properties:
        id:
          type: integer
          format: int64
          readOnly: true
        clienteId:
          type: integer
          format: int64
          readOnly: true
        clienteNome:
          type: string
          readOnly: true
        categoriaId:
          type: integer
          format: int64
        categoriaNome:
          type: string
          readOnly: true
        descricao:
          type: string
        cidade:
          type: string
        estado:
          type: string
          minLength: 2
          maxLength: 2
        status:
          type: string
          enum: [ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA]
          readOnly: true
        dataCriacao:
          type: string
          format: date-time
          readOnly: true
```

---

## Endpoint 3 — Aceite de Proposta (equivalente ao Checkout de Serviço)

### `PATCH /api/propostas/{id}/aceitar`

🔒 **JWT** — Exclusivo para o cliente dono da solicitação à qual a proposta pertence.

Aceita uma proposta de prestador. Esta é a ação de "checkout" do marketplace: ao aceitar, o serviço é formalmente contratado, a proposta muda para `ACEITA` e a solicitação avança para `EM_ANDAMENTO`.

**Parâmetros de path:**

- `id` *(Long, obrigatório)* — ID da proposta a ser aceita

**Cabeçalhos obrigatórios:**

```
Authorization: Bearer <token>
```

**Exemplo de requisição:**

```http
PATCH /api/propostas/18/aceitar
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

**Resposta de sucesso — `200 OK`:**

```json
{
  "id": 18,
  "solicitacaoId": 47,
  "prestadorId": 12,
  "prestadorNome": "Maria Santos",
  "valor": 350.00,
  "mensagem": "Posso realizar o serviço na próxima semana. Incluo garantia de 90 dias na mão de obra.",
  "status": "ACEITA",
  "dataEnvio": "2026-05-10T14:30:00"
}
```

**Efeitos colaterais após aceite:**

- `Proposta.status` → `ACEITA`
- `SolicitacaoServico.status` → `EM_ANDAMENTO`
- A solicitação deixa de aparecer na listagem de solicitações abertas para prestadores

**Possíveis erros:**

- `400 Bad Request` — proposta já foi aceita ou recusada anteriormente ("Esta proposta já foi processada")
- `401 Unauthorized` — token ausente ou inválido
- `403 Forbidden` — usuário não é o dono da solicitação
- `404 Not Found` — proposta não encontrada

**Especificação OpenAPI:**

```yaml
/api/propostas/{id}/aceitar:
  patch:
    tags:
      - Propostas
    summary: Aceitar proposta de prestador
    security:
      - bearer-jwt: []
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: integer
          format: int64
    responses:
      '200':
        description: Proposta aceita com sucesso
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PropostaDTO'
      '400':
        description: Proposta já processada
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ErrorResponse'
      '401':
        description: Não autenticado
      '403':
        description: Usuário não é o dono da solicitação
      '404':
        description: Proposta não encontrada

    # Nota: implementação atual não valida ownership.
    # A verificação de que o usuário logado é dono da solicitação
    # está pendente de implementação (débito técnico C2).

components:
  schemas:
    PropostaDTO:
      type: object
      properties:
        id:
          type: integer
          format: int64
        solicitacaoId:
          type: integer
          format: int64
        prestadorId:
          type: integer
          format: int64
        prestadorNome:
          type: string
        valor:
          type: number
          format: double
        mensagem:
          type: string
        status:
          type: string
          enum: [ENVIADA, ACEITA, RECUSADA]
        dataEnvio:
          type: string
          format: date-time

    ErrorResponse:
      type: object
      properties:
        timestamp:
          type: string
          format: date-time
        status:
          type: integer
        error:
          type: string
        message:
          type: string
        path:
          type: string

  securitySchemes:
    bearer-jwt:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

---

## Resumo de todos os endpoints

### `/api/auth`
- `POST /api/auth/register` — Registro de usuário (público)
- `POST /api/auth/login` — Login e obtenção de token JWT (público)

### `/api/categorias`
- `GET /api/categorias` — Listar categorias paginadas (público)
- `GET /api/categorias/{id}` — Buscar categoria por ID (público)
- `POST /api/categorias` — Criar categoria (requer ADMIN — pendente)
- `PUT /api/categorias/{id}` — Atualizar categoria (requer ADMIN — pendente)
- `DELETE /api/categorias/{id}` — Deletar categoria (requer ADMIN — pendente)

### `/api/prestadores`
- `GET /api/prestadores` — Buscar prestadores com filtros (público)
- `GET /api/prestadores/{id}` — Buscar prestador por ID (público)
- `GET /api/prestadores/me` — Buscar perfil do prestador logado (🔒 JWT)
- `POST /api/prestadores` — Criar ou atualizar perfil (🔒 JWT — PRESTADOR)

### `/api/solicitacoes`
- `POST /api/solicitacoes` — Criar solicitação (🔒 JWT — CLIENTE)
- `GET /api/solicitacoes/{id}` — Buscar por ID (🔒 JWT)
- `GET /api/solicitacoes/minhas` — Listar do cliente logado (🔒 JWT — CLIENTE)
- `GET /api/solicitacoes/abertas` — Listar abertas com filtros (🔒 JWT)
- `PATCH /api/solicitacoes/{id}/status` — Atualizar status (🔒 JWT — dono)
- `DELETE /api/solicitacoes/{id}` — Cancelar solicitação (🔒 JWT — dono)

### `/api/propostas`
- `POST /api/propostas` — Enviar proposta (🔒 JWT — PRESTADOR)
- `GET /api/propostas/solicitacao/{id}` — Propostas de uma solicitação (🔒 JWT)
- `GET /api/propostas/minhas` — Propostas do prestador logado (🔒 JWT — PRESTADOR)
- `PATCH /api/propostas/{id}/aceitar` — Aceitar proposta (🔒 JWT — dono da solicitação)
- `PATCH /api/propostas/{id}/recusar` — Recusar proposta (🔒 JWT — dono da solicitação)

### `/api/avaliacoes`
- `POST /api/avaliacoes` — Criar avaliação (🔒 JWT — CLIENTE dono da solicitação CONCLUIDA)
- `GET /api/avaliacoes/prestador/{id}` — Listar avaliações de um prestador (público)

### `/api/users`
- `GET /api/users/me` — Dados do usuário logado (🔒 JWT)
- `PUT /api/users/me` — Atualizar dados do usuário logado (🔒 JWT)
- `DELETE /api/users/me` — Desativar conta (🔒 JWT)
