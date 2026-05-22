package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusDenuncia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarDenunciaDTO {

    @NotNull(message = "O status é obrigatório")
    private StatusDenuncia status;

    private String observacaoAdmin;
}
