package com.marketplace.servicos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComprarCreditosDTO {
    @NotNull(message = "Informe o pacote")
    private Long pacoteId;
}
