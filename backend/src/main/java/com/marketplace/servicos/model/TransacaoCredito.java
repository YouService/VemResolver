package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes_credito")
@Data
@NoArgsConstructor
public class TransacaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaCredito conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacaoCredito tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTransacaoCredito status;

    // Positivo em compras, negativo em consumos, zero em consumos gratuitos
    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "valor_centavos")
    private Long valorCentavos;

    // Preenchido quando a transação é aprovada
    @Column(name = "saldo_apos")
    private Integer saldoApos;

    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacote_id")
    private PacoteCredito pacote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposta_id")
    private Proposta proposta;

    // Nome do gateway que processou a compra (ex.: "ficticio", "mercadopago")
    private String gateway;

    @Column(name = "referencia_externa", unique = true)
    private String referenciaExterna;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now().withNano(0);
    }
}
