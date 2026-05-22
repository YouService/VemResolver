package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "denuncias")
@Data
@NoArgsConstructor
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denunciante_id", nullable = false)
    private User denunciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denunciado_id", nullable = false)
    private User denunciado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDenuncia tipo;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDenuncia status = StatusDenuncia.PENDENTE;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_revisao")
    private LocalDateTime dataRevisao;

    @Column(name = "observacao_admin")
    private String observacaoAdmin;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now().withNano(0);
    }
}
