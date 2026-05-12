# Especificação de Regras de Negócio — VemResolver

> **Versão:** 1.0  
> **Data:** 2026-05-11  
> **Status:** Em vigor

---

## 1. Glossário

- **Cliente** — usuário com `tipo = CLIENTE` que abre solicitações de serviço
- **Prestador** — usuário com `tipo = PRESTADOR` que possui perfil profissional e envia propostas
- **Solicitação** — pedido de serviço criado por um cliente, descrevendo o trabalho, localização e categoria
- **Proposta** — oferta enviada por um prestador para uma solicitação, contendo valor e mensagem
- **Avaliação** — nota e comentário que o cliente atribui ao prestador após conclusão do serviço

---

## 2. Cadastro e Validação de Usuários

### 2.1 Regras de registro

- O campo `email` deve ser único no sistema. Tentativas de cadastro com email já existente retornam erro `400 Bad Request` com mensagem "Email já cadastrado"
- O campo `tipo` deve ser informado no ato do cadastro (`CLIENTE` ou `PRESTADOR`) e não pode ser alterado posteriormente pelo próprio usuário
- O `login` é gerado automaticamente a partir do prefixo do email (parte antes do `@`). Em caso de conflito, um sufixo numérico é acrescentado em incremento (`joao`, `joao1`, `joao2`, etc.)
- A senha é armazenada exclusivamente como hash BCrypt — nunca em texto plano

### 2.2 Perfil do prestador

- Um usuário com `tipo = PRESTADOR` pode, opcionalmente, completar um perfil profissional
- O perfil é identificado como incompleto enquanto não possuir: `descricaoPerfil` (mínimo 50 caracteres), pelo menos uma `categoria` selecionada, `cidade` e `estado`
- Um prestador sem perfil completo não aparece nos resultados de busca
- O perfil é criado ou atualizado via `POST /api/prestadores` — o mesmo endpoint realiza upsert baseado no `usuarioId` extraído do token JWT
- Cada usuário pode ter no máximo um perfil de prestador (relação `OneToOne` entre `User` e `Prestador`)

### 2.3 Desativação de conta

- A exclusão de conta é um soft delete: o campo `ativo` é marcado como `false`, o registro permanece no banco
- Usuários com `ativo = false` são rejeitados pelo `UserDetailsService` no processo de autenticação (`isEnabled()` retorna `false`)
- Solicitações e avaliações associadas ao usuário desativado são preservadas para fins de auditoria

---

## 3. Fluxo de Solicitação e Contratação de Serviço

Este é o fluxo central do marketplace. A sequência abaixo descreve o caminho feliz completo:

```
[Cliente]                    [Sistema]                    [Prestador]
    │                            │                              │
    │── Cria Solicitação ───────►│                              │
    │                            │── Status: ABERTA             │
    │                            │──────────────────────────►   │
    │                            │              Vê solicitação  │
    │                            │◄── Envia Proposta ──────────│
    │                            │                              │
    │◄── Recebe Proposta ────────│                              │
    │                            │                              │
    │── Aceita Proposta ────────►│                              │
    │                            │── Status proposta: ACEITA    │
    │                            │── Status solicitação:        │
    │                            │   EM_ANDAMENTO               │
    │                            │──────────────────────────►   │
    │                            │              Executa serviço │
    │── Marca como Concluído ───►│                              │
    │                            │── Status: CONCLUIDA          │
    │                            │                              │
    │── Avalia Prestador ───────►│                              │
    │                            │── Atualiza média do prestador│
```

### 3.1 Criação de solicitação

- Exclusivo para usuários com `tipo = CLIENTE`; prestadores não podem criar solicitações
- Campos obrigatórios: `categoriaId`, `descricao`, `cidade`, `estado`
- O `status` inicial é sempre `ABERTA` — não pode ser informado pelo cliente na criação
- Uma solicitação `ABERTA` aceita novas propostas de múltiplos prestadores
- Uma solicitação recém-criada não possui orçamento de referência; o valor é definido pelo prestador na proposta

### 3.2 Envio de proposta

- Exclusivo para usuários com `tipo = PRESTADOR` que possuam perfil cadastrado
- Um prestador pode enviar no máximo uma proposta por solicitação. Tentativas de envio duplicado retornam erro com a mensagem "Você já enviou uma proposta para esta solicitação"
- Apenas solicitações com `status = ABERTA` aceitam novas propostas
- A proposta é criada com `status = ENVIADA`
- Campos obrigatórios: `solicitacaoId`, `valor` (BigDecimal positivo), `mensagem`

### 3.3 Aceite e recusa de proposta

- Exclusivo para o cliente dono da solicitação
- Ao aceitar uma proposta:
  - O `status` da proposta muda para `ACEITA`
  - O `status` da solicitação muda para `EM_ANDAMENTO`
  - As demais propostas da mesma solicitação permanecem em `ENVIADA` (não são automaticamente recusadas — decisão de produto em aberto)
- Ao recusar uma proposta, apenas o `status` da proposta muda para `RECUSADA`; a solicitação permanece `ABERTA`
- Propostas com `status` diferente de `ENVIADA` não podem ser aceitas ou recusadas novamente (são imutáveis)

### 3.4 Conclusão do serviço

- O status `CONCLUIDA` é aplicado via `PATCH /api/solicitacoes/{id}/status`
- A solicitação deve estar em `EM_ANDAMENTO` para ser concluída
- Apenas o cliente dono da solicitação pode marcar o serviço como concluído
- Após a conclusão, o cliente pode avaliar o prestador

---

## 4. Política de Cancelamento

### 4.1 Cancelamento de solicitação pelo cliente

- Uma solicitação pode ser cancelada **somente** quando seu `status` é `ABERTA`
- O cancelamento é realizado via `DELETE /api/solicitacoes/{id}` — que na implementação atual aplica soft delete via status `CANCELADA`
- Apenas o cliente dono da solicitação pode cancelá-la. Tentativas de outro usuário retornam `403 Forbidden`
- Solicitações em `EM_ANDAMENTO` ou `CONCLUIDA` não podem ser canceladas pelo fluxo atual (necessita aprovação do outro lado, funcionalidade a implementar)
- Propostas associadas à solicitação cancelada não têm seu status alterado automaticamente (débito técnico identificado)

### 4.2 Recusa de proposta

- Qualquer proposta com `status = ENVIADA` pode ser recusada pelo cliente dono da solicitação
- Não há cancelamento de proposta pelo prestador (funcionalidade não implementada)

### 4.3 Regras de imutabilidade de estado

| Status atual da solicitação | Pode cancelar? | Pode concluir? | Aceita novas propostas? |
|-----------------------------|---------------|----------------|------------------------|
| ABERTA | Sim | Não | Sim |
| EM_ANDAMENTO | Não | Sim | Não |
| CONCLUIDA | Não | Não | Não |
| CANCELADA | Não | Não | Não |

---

## 5. Avaliação de Prestadores

- A avaliação só pode ser criada quando a `SolicitacaoServico` tem `status = CONCLUIDA`
- Apenas o cliente que criou a solicitação pode avaliá-la
- Cada solicitação permite no máximo uma avaliação. Tentativas duplicadas retornam erro com "Esta solicitação já foi avaliada"
- A nota deve estar no intervalo 1–5 (validação a implementar na camada de Bean Validation)
- Após cada nova avaliação, a `avaliacaoMedia` e o `totalAvaliacoes` do prestador são recalculados e persistidos imediatamente

---

## 6. Categorias de Serviço

- Categorias são dados de referência gerenciados por administradores
- Um prestador pode estar associado a múltiplas categorias (relação `ManyToMany`)
- Uma solicitação pertence a exatamente uma categoria
- As 10 categorias iniciais são criadas automaticamente pelo `DataSeeder` na primeira inicialização do sistema: Eletricista, Encanador, Pintor, Pedreiro, Marceneiro, Jardineiro, Diarista, Professor Particular, Mecânico, Desenvolvedor

---

## 7. Regras de Autorização por Recurso

| Recurso | Criar | Ler | Atualizar | Deletar |
|---------|-------|-----|-----------|---------|
| Solicitação | CLIENTE | Autenticado | Dono (cliente) | Dono (cliente) — somente ABERTA |
| Proposta | PRESTADOR (com perfil) | Autenticado | Dono da solicitação | — |
| Avaliação | CLIENTE (dono da solicitação CONCLUIDA) | Público | — | — |
| Perfil Prestador | PRESTADOR | Público | Dono (prestador) | — |
| Categoria | ADMIN | Público | ADMIN | ADMIN |
| Usuário | Público (registro) | Próprio usuário | Próprio usuário | Próprio usuário |
