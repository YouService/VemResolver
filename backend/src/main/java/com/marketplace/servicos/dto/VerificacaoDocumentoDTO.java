package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusVerificacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacaoDocumentoDTO {
    private Long id;
    private Long prestadorId;
    private String prestadorNome;
    private StatusVerificacao status;
    private String dataEnvio;
    private String dataRevisao;
    private String observacaoAdmin;
}
