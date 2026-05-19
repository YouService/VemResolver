package com.marketplace.servicos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagens_chat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoServico solicitacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposta_id")
    private Proposta proposta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private User remetente;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "url_anexo", columnDefinition = "TEXT")
    private String urlAnexo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private Boolean lida = false;
}
