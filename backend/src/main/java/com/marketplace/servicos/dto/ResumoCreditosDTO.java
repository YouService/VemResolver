package com.marketplace.servicos.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class ResumoCreditosDTO {
    private Integer saldo;
    private Integer gratuitosRestantes;
    private Integer gratuitosTotal;
    private Long totalGastoCentavos;
    private Integer limiteAlertaSaldo;
    private Boolean saldoBaixo;
    private Boolean pagamentoFicticio;
    private Page<TransacaoCreditoDTO> transacoes;
}
