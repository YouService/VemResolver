package com.marketplace.servicos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class TesteEmailDTO {

    @NotBlank
    @Email
    private String emailDestino;

    public String getEmailDestino() { return emailDestino; }
    public void setEmailDestino(String emailDestino) { this.emailDestino = emailDestino; }
}
