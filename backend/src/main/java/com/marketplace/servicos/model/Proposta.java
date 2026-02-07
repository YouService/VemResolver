package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "propostas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proposta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private SolicitacaoServico solicitacao;
    
    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Column(columnDefinition = "TEXT")
    private String mensagem;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProposta status = StatusProposta.ENVIADA;
    
    @Column(nullable = false)
    private LocalDateTime dataEnvio;
    
    @PrePersist
    protected void onCreate() {
        dataEnvio = LocalDateTime.now();
        if (status == null) {
            status = StatusProposta.ENVIADA;
        }
    }
}
