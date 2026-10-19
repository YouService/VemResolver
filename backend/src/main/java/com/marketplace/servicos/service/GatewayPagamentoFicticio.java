package com.marketplace.servicos.service;

import com.marketplace.servicos.model.StatusTransacaoCredito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gateway de pagamento fictício: aprova toda cobrança na hora, sem movimentar dinheiro.
 * Nunca é carregado com o profile "prod".
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "pagamento.gateway", havingValue = "ficticio", matchIfMissing = true)
public class GatewayPagamentoFicticio implements GatewayPagamento {

    private static final Logger log = LoggerFactory.getLogger(GatewayPagamentoFicticio.class);

    @Override
    public String nome() {
        return "ficticio";
    }

    @Override
    public boolean ficticio() {
        return true;
    }

    @Override
    public ResultadoCobranca criarCobranca(Cobranca cobranca) {
        String referencia = "FICT-" + UUID.randomUUID();
        log.warn("[Pagamento FICTÍCIO] cobrança aprovada automaticamente: transacao={} valor={} centavos ref={}",
                cobranca.transacaoId(), cobranca.valorCentavos(), referencia);
        return new ResultadoCobranca(referencia, StatusTransacaoCredito.APROVADA, null);
    }
}
