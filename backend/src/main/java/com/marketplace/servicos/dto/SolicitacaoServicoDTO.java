package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusSolicitacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoServicoDTO {
    private Long id;
    
    private Long clienteId;
    private String clienteNome;
    
    @NotNull(message = "Categoria é obrigatória")
    private Long categoriaId;
    private String categoriaNome;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;
    
    @NotBlank(message = "Estado é obrigatório")
    private String estado;
    
    private StatusSolicitacao status;
    private LocalDateTime dataCriacao;
}
