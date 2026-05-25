package com.marketplace.servicos.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {
    private final MeterRegistry registry;
    private final Counter propostasEnviadas;
    private final Counter contratosFinalizados;
    private final Counter loginsFalhos;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
        this.propostasEnviadas = Counter.builder("vemresolver.propostas.enviadas")
            .description("Total de propostas enviadas").register(registry);
        this.contratosFinalizados = Counter.builder("vemresolver.contratos.finalizados")
            .description("Total de contratos finalizados").register(registry);
        this.loginsFalhos = Counter.builder("vemresolver.logins.falhos")
            .description("Total de tentativas de login falhadas").register(registry);
    }

    public void incrementarPropostaEnviada() {
        propostasEnviadas.increment();
    }

    public void incrementarContratoFinalizado() {
        contratosFinalizados.increment();
    }

    public void incrementarLoginFalho() {
        loginsFalhos.increment();
    }
}
