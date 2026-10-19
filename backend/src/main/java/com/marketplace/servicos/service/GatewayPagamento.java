package com.marketplace.servicos.service;

import com.marketplace.servicos.model.StatusTransacaoCredito;

/**
 * Ponto de integração com o provedor de pagamento.
 *
 * Hoje só existe {@link GatewayPagamentoFicticio}. Para usar o Mercado Pago,
 * basta criar outra implementação ativada por {@code pagamento.gateway=mercadopago}
 * e, no webhook do provedor, chamar {@link CreditoService#confirmarPagamento}.
 */
public interface GatewayPagamento {

    String nome();

    /** true quando os pagamentos não são reais (ambiente de testes). */
    boolean ficticio();

    ResultadoCobranca criarCobranca(Cobranca cobranca);

    record Cobranca(Long transacaoId, String descricao, long valorCentavos, String emailPagador) {}

    /**
     * @param status       APROVADA quando o pagamento foi confirmado na hora,
     *                     PENDENTE quando a confirmação chega depois (webhook)
     * @param urlCheckout  página de pagamento do provedor, se houver
     */
    record ResultadoCobranca(String referenciaExterna, StatusTransacaoCredito status, String urlCheckout) {}
}
