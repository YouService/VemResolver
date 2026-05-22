package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.TipoDenuncia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CriarDenunciaDTO {

    @NotNull(message = "O ID do denunciado é obrigatório")
    private Long denunciadoId;

    @NotNull(message = "O tipo da denúncia é obrigatório")
    private TipoDenuncia tipo;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(min = 10, max = 1000, message = "A descrição deve ter entre 10 e 1000 caracteres")
    private String descricao;
}
