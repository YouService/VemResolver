package com.marketplace.servicos.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservabilidadeTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    @DisplayName("MetricsService: incrementa contador de propostas enviadas")
    void metricsService_IncrementaPropostas() {
        Counter counter = meterRegistry.find("vemresolver.propostas.enviadas").counter();
        assertNull(counter, "Counter deve estar null antes do incremento");

        metricsService.incrementarPropostaEnviada();

        counter = meterRegistry.find("vemresolver.propostas.enviadas").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());

        metricsService.incrementarPropostaEnviada();
        assertEquals(2.0, counter.count());
    }

    @Test
    @DisplayName("MetricsService: incrementa contador de contratos finalizados")
    void metricsService_IncrementaContratosFinalizados() {
        metricsService.incrementarContratoFinalizado();
        metricsService.incrementarContratoFinalizado();
        metricsService.incrementarContratoFinalizado();

        Counter counter = meterRegistry.find("vemresolver.contratos.finalizados").counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count());
    }

    @Test
    @DisplayName("MetricsService: incrementa contador de logins falhados")
    void metricsService_IncrementaLoginsFalhos() {
        metricsService.incrementarLoginFalho();

        Counter counter = meterRegistry.find("vemresolver.logins.falhos").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());

        metricsService.incrementarLoginFalho();
        metricsService.incrementarLoginFalho();
        assertEquals(3.0, counter.count());
    }

    @Test
    @DisplayName("MetricsService: múltiplos counters registrados corretamente")
    void metricsService_MultiplosCountersRegistrados() {
        metricsService.incrementarPropostaEnviada();
        metricsService.incrementarPropostaEnviada();
        metricsService.incrementarContratoFinalizado();
        metricsService.incrementarLoginFalho();
        metricsService.incrementarLoginFalho();

        Counter propostas = meterRegistry.find("vemresolver.propostas.enviadas").counter();
        Counter contratos = meterRegistry.find("vemresolver.contratos.finalizados").counter();
        Counter logins = meterRegistry.find("vemresolver.logins.falhos").counter();

        assertEquals(2.0, propostas.count());
        assertEquals(1.0, contratos.count());
        assertEquals(2.0, logins.count());
    }
}
