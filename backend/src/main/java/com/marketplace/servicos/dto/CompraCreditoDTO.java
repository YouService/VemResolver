package com.marketplace.servicos.dto;

import lombok.Data;

@Data
public class CompraCreditoDTO {
    private TransacaoCreditoDTO transacao;
    private Integer saldoAtual;
    // Página de pagamento do provedor (null quando o pagamento é confirmado na hora)
    private String urlCheckout;
}
