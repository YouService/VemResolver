package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusProposta;
import com.marketplace.servicos.model.StatusSolicitacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropostaDTO {
    private Long id;

    @NotNull(message = "ID da solicitação é obrigatório")
    private Long solicitacaoId;

    private Long prestadorId;
    private String prestadorNome;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    private String mensagem;

    private StatusProposta status;
    private LocalDateTime dataEnvio;

    // Dados da solicitação — populados nas respostas, ignorados no input
    private String solicitacaoDescricao;
    private StatusSolicitacao solicitacaoStatus;
    private String clienteNome;
    private Long clienteId;
    private String cidade;
    private String estado;
    private String categoriaNome;
}
