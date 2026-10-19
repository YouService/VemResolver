package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pacotes_credito")
@Data
@NoArgsConstructor
public class PacoteCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "quantidade_creditos", nullable = false)
    private Integer quantidadeCreditos;

    // Valores monetários sempre em centavos para evitar erros de arredondamento
    @Column(name = "preco_centavos", nullable = false)
    private Long precoCentavos;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now().withNano(0);
    }
}
