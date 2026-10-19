package com.marketplace.servicos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PacoteCreditoDTO {
    private Long id;

    @NotBlank(message = "Nome do pacote é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    private String descricao;

    @NotNull(message = "Quantidade de créditos é obrigatória")
    @Min(value = 1, message = "O pacote deve ter pelo menos 1 crédito")
    private Integer quantidadeCreditos;

    @NotNull(message = "Preço é obrigatório")
    @Min(value = 0, message = "Preço não pode ser negativo")
    private Long precoCentavos;

    private Boolean ativo = true;
}
