---
name: security-patterns
description: Ativar quando trabalhar com Spring Security, JWT, roles de usuário, endpoints protegidos, validação de tipo de usuário, CORS, Swagger, DataSeeder, ou qualquer aspecto de segurança do VemResolver. Complementa o agent security-engineer com os padrões específicos do projeto.
---

# Padrões de Segurança — VemResolver

## Mapa de endpoints e autenticação

### Endpoints públicos (sem token)
```
POST /api/auth/register
POST /api/auth/login
GET  /api/categorias
GET  /api/categorias/{id}
GET  /api/prestadores
GET  /api/prestadores/{id}
GET  /api/avaliacoes/prestador/{id}
```

### Endpoints autenticados por role

| Endpoint | Role obrigatória | Verificação adicional |
|---|---|---|
| POST /api/solicitacoes | CLIENTE | tipo == CLIENTE |
| GET /api/solicitacoes/minhas | CLIENTE | — |
| PATCH /api/solicitacoes/{id}/status | CLIENTE | ownership |
| DELETE /api/solicitacoes/{id} | CLIENTE | ownership |
| POST /api/propostas | PRESTADOR | tipo == PRESTADOR |
| GET /api/propostas/minhas | PRESTADOR | — |
| PATCH /api/propostas/{id}/aceitar | CLIENTE | ownership da solicitacao |
| PATCH /api/propostas/{id}/recusar | CLIENTE | ownership da solicitacao |
| POST /api/avaliacoes | CLIENTE | proposta concluida |
| GET /api/users/me | Qualquer | — |
| PUT /api/users/me | Qualquer | campos restritos |
| POST /api/categorias | ADMIN | — |
| PUT /api/categorias/{id} | ADMIN | — |
| DELETE /api/categorias/{id} | ADMIN | — |

## Verificacao de tipo de usuario

```java
public SolicitacaoServico criar(SolicitacaoRequestDTO dto, String emailUsuario) {
    User usuario = userRepository.findByEmail(emailUsuario)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

    if (usuario.getTipo() != TipoUsuario.CLIENTE) {
        throw new BusinessException("Apenas clientes podem criar solicitacoes");
    }
}

public Proposta enviarProposta(PropostaRequestDTO dto, String emailUsuario) {
    User usuario = userRepository.findByEmail(emailUsuario)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

    if (usuario.getTipo() != TipoUsuario.PRESTADOR) {
        throw new BusinessException("Apenas prestadores podem enviar propostas");
    }
}
```

## DataSeeder apenas em dev

```java
@Component
@Profile({"dev", "test"})
public class DataSeeder implements CommandLineRunner {
    // seed de categorias e usuarios de teste
}
```

## UserResponseDTO campos permitidos

```java
// NUNCA incluir: senha, rg, cpf, dataExpedicao
public record UserResponseDTO(
    Long id,
    String nome,
    String email,
    String login,
    String telefone,
    TipoUsuario tipo,
    LocalDateTime dataCadastro,
    Boolean ativo
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
            user.getId(), user.getNome(), user.getEmail(),
            user.getLogin(), user.getTelefone(), user.getTipo(),
            user.getDataCadastro(), user.getAtivo()
        );
    }
}
```

## UserUpdateDTO campos editaveis

```java
// APENAS campos que o usuario pode editar
public record UserUpdateDTO(
    @Size(min = 2, max = 100) String nome,
    @Pattern(regexp = "\\d{10,11}") String telefone
) {}
```

## Regras criticas nunca violar

- Senha SEMPRE com BCrypt
- Entidade User NUNCA retornada diretamente na API
- CORS SEMPRE via variavel de ambiente
- DataSeeder SEMPRE com @Profile("dev")
- Swagger SEMPRE restrito ao profile dev
- Ownership SEMPRE verificado no Service antes de PATCH/DELETE
