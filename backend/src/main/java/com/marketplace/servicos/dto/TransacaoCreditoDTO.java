package com.marketplace.servicos.dto;

import com.marketplace.servicos.model.StatusTransacaoCredito;
import com.marketplace.servicos.model.TipoTransacaoCredito;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransacaoCreditoDTO {
    private Long id;
    private TipoTransacaoCredito tipo;
    private StatusTransacaoCredito status;
    private Integer quantidade;
    private Long valorCentavos;
    private Integer saldoApos;
    private String descricao;
    private String pacoteNome;
    private Long propostaId;
    private String gateway;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataConfirmacao;
}
