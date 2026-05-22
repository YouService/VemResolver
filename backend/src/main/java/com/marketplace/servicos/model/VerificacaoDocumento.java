package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verificacoes_documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacaoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(name = "caminho_doc", nullable = false, columnDefinition = "TEXT")
    private String caminhoDoc;

    @Column(name = "caminho_selfie", nullable = false, columnDefinition = "TEXT")
    private String caminhoSelfie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVerificacao status = StatusVerificacao.PENDENTE;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();

    @Column(name = "data_revisao")
    private LocalDateTime dataRevisao;

    @Column(name = "observacao_admin", columnDefinition = "TEXT")
    private String observacaoAdmin;
}
