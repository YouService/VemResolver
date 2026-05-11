---
name: marketplace-domain
description: Ativar quando trabalhar com entidades de domínio do VemResolver (Cliente, Profissional, Servico, Proposta, Contrato, Avaliacao), regras de negócio do marketplace, ou ao criar/editar qualquer entidade JPA do projeto.
---

# Domínio VemResolver — Marketplace de Serviços Locais

## Modelo de domínio

### Entidades JPA e relacionamentos

```
Cliente (1) ──── (N) Servico
Profissional (1) ──── (N) Proposta
Servico (1) ──── (N) Proposta
Proposta (1) ──── (0..1) Contrato
Contrato (1) ──── (0..2) Avaliacao
Profissional (N) ──── (N) Categoria
```

### Cliente
```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;          // único
    private String telefone;
    private String senha;          // BCrypt
    private String cep;
    private String cidade;
    private String estado;
    @Enumerated(EnumType.STRING)
    private StatusUsuario status;  // ATIVO, INATIVO, BLOQUEADO
    private LocalDateTime criadoEm;
}
```

### Profissional
```java
@Entity
@Table(name = "profissionais")
public class Profissional {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String senha;
    private String descricaoPerfil;
    private String cep;
    private String cidade;
    private String estado;
    private Integer raioAtendimentoKm;   // raio de atuação
    private Double avaliacaoMedia;        // calculado automaticamente
    private Integer totalAvaliacoes;
    @ManyToMany
    @JoinTable(name = "profissional_categorias")
    private List<Categoria> categorias;
    @Enumerated(EnumType.STRING)
    private StatusUsuario status;
    private LocalDateTime criadoEm;
}
```

### Categoria
```java
@Entity
@Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;           // ex: "Encanador", "Eletricista"
    private String descricao;
    private String icone;          // nome do ícone para o frontend
    private Boolean ativa;
}
```

### Servico (necessidade postada pelo cliente)
```java
@Entity
@Table(name = "servicos")
public class Servico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Cliente cliente;
    @ManyToOne
    private Categoria categoria;
    private String titulo;
    private String descricao;
    private String cep;
    private String cidade;
    private String estado;
    @Enumerated(EnumType.STRING)
    private StatusServico status;  // ABERTO, EM_ANDAMENTO, CONCLUIDO, CANCELADO
    private LocalDateTime criadoEm;
    private LocalDateTime expiradoEm;  // serviços expiram após 30 dias sem contrato
}
```

### Proposta
```java
@Entity
@Table(name = "propostas")
public class Proposta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Servico servico;
    @ManyToOne
    private Profissional profissional;
    private BigDecimal valorProposto;
    private Integer prazoEmDias;
    private String mensagem;
    @Enumerated(EnumType.STRING)
    private StatusProposta status;  // PENDENTE, ACEITA, RECUSADA, CANCELADA
    private LocalDateTime criadoEm;
}
```

### Contrato
```java
@Entity
@Table(name = "contratos")
public class Contrato {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Proposta proposta;
    private BigDecimal valorFinal;
    private LocalDate prazoEntrega;
    @Enumerated(EnumType.STRING)
    private StatusContrato status;  // ATIVO, CONCLUIDO, CANCELADO, DISPUTADO
    private LocalDateTime criadoEm;
    private LocalDateTime concluidoEm;
}
```

### Avaliacao
```java
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Contrato contrato;
    @Enumerated(EnumType.STRING)
    private TipoAvaliador tipoAvaliador;  // CLIENTE, PROFISSIONAL
    private Integer nota;                  // 1 a 5
    private String comentario;
    private LocalDateTime criadoEm;
}
```

## Regras de negócio críticas

### Propostas
- Um profissional pode enviar apenas UMA proposta por Servico
- Apenas Servicos com status ABERTO aceitam propostas
- Ao aceitar uma proposta, TODAS as outras propostas do mesmo Servico são RECUSADAS automaticamente
- O Servico muda para EM_ANDAMENTO quando uma proposta é ACEITA

### Contratos
- Contrato só é criado quando Cliente aceita uma Proposta
- Ao criar o Contrato, a Proposta muda para ACEITA e o Servico para EM_ANDAMENTO
- Contrato só pode ser concluído pelo Cliente
- Após conclusão do Contrato, ambos têm 7 dias para avaliar

### Avaliações
- Cada parte avalia apenas uma vez por Contrato
- Nota mínima: 1, máxima: 5
- Ao receber avaliação, recalcular `avaliacaoMedia` e `totalAvaliacoes` do Profissional
- Avaliações são públicas e visíveis no perfil do Profissional

### Geolocalização
- Matching de profissionais considera: mesma cidade OU dentro do raioAtendimentoKm
- Profissionais são notificados apenas de Servicos dentro do seu raio
- Priorizar profissionais mais próximos na listagem

## Enums

```java
public enum StatusUsuario { ATIVO, INATIVO, BLOQUEADO }
public enum StatusServico { ABERTO, EM_ANDAMENTO, CONCLUIDO, CANCELADO }
public enum StatusProposta { PENDENTE, ACEITA, RECUSADA, CANCELADA }
public enum StatusContrato { ATIVO, CONCLUIDO, CANCELADO, DISPUTADO }
public enum TipoAvaliador { CLIENTE, PROFISSIONAL }
```

## Migrations Flyway — ordem esperada
```
V1__create_categorias.sql
V2__create_clientes.sql
V3__create_profissionais.sql
V4__create_profissional_categorias.sql
V5__create_servicos.sql
V6__create_propostas.sql
V7__create_contratos.sql
V8__create_avaliacoes.sql
V9__add_indexes.sql
```
