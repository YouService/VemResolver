package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private SolicitacaoServico solicitacao;
    
    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;
    
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAvaliacao tipo = TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR;

    @Column(nullable = false)
    private Integer nota;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
