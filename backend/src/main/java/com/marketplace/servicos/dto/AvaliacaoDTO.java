package com.marketplace.servicos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoDTO {
    private Long id;
    
    @NotNull(message = "ID da solicitação é obrigatório")
    private Long solicitacaoId;
    
    @NotNull(message = "ID do prestador é obrigatório")
    private Long prestadorId;
    
    private Long clienteId;
    private String clienteNome;
    
    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota mínima é 1")
    @Max(value = 5, message = "Nota máxima é 5")
    private Integer nota;
    
    private String comentario;
}
