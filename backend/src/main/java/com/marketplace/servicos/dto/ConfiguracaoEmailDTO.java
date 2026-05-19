package com.marketplace.servicos.dto;

import java.time.LocalDateTime;

public class ConfiguracaoEmailDTO {

    private String brevoApiKey;
    private String emailRemetente;
    private String nomeRemetente;
    private Boolean ativo;
    private LocalDateTime atualizadoEm;
    private Boolean configurado;

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

    public Boolean getConfigurado() { return configurado; }
    public void setConfigurado(Boolean configurado) { this.configurado = configurado; }
}
