package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contas_credito")
@Data
@NoArgsConstructor
public class ContaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "prestador_id", nullable = false, unique = true)
    private Prestador prestador;

    @Column(nullable = false)
    private Integer saldo = 0;

    // Aceites cobertos pelo tier gratuito (não debitam saldo)
    @Column(name = "gratuitos_usados", nullable = false)
    private Integer gratuitosUsados = 0;

    @Column(name = "total_gasto_centavos", nullable = false)
    private Long totalGastoCentavos = 0L;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        dataAtualizacao = LocalDateTime.now().withNano(0);
    }
}
