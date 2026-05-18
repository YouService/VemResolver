package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusSolicitacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitacaoAdminDTO {
    private Long id;
    private String clienteNome;
    private String clienteEmail;
    private String categoriaNome;
    private String descricao;
    private String cidade;
    private String estado;
    private StatusSolicitacao status;
    private LocalDateTime dataCriacao;
    private long totalPropostas;
}
