package com.marketplace.servicos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracoes_email")
public class ConfiguracaoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brevo_api_key")
    private String brevoApiKey;

    @Column(name = "email_remetente")
    private String emailRemetente;

    @Column(name = "nome_remetente")
    private String nomeRemetente;

    @Column(nullable = false)
    private Boolean ativo = false;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBrevoApiKey() { return brevoApiKey; }
    public void setBrevoApiKey(String brevoApiKey) { this.brevoApiKey = brevoApiKey; }

    public String getEmailRemetente() { return emailRemetente; }
    public void setEmailRemetente(String emailRemetente) { this.emailRemetente = emailRemetente; }

    public String getNomeRemetente() { return nomeRemetente; }
    public void setNomeRemetente(String nomeRemetente) { this.nomeRemetente = nomeRemetente; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
