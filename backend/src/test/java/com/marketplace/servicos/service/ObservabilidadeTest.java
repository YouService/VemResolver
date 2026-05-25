package com.marketplace.servicos.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservabilidadeTest {

    private MetricsService metricsService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void testMetricsServiceRegistraCounterPropostas() {
        metricsService.incrementarPropostaEnviada();
        metricsService.incrementarPropostaEnviada();

        Counter counter = meterRegistry.find("vemresolver.propostas.enviadas").counter();
        assertNotNull(counter);
        assertEquals(2.0, counter.count());
    }

    @Test
    void testMetricsServiceRegistraCounterContratos() {
        metricsService.incrementarContratoFinalizado();
        metricsService.incrementarContratoFinalizado();
        metricsService.incrementarContratoFinalizado();

        Counter counter = meterRegistry.find("vemresolver.contratos.finalizados").counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count());
    }

    @Test
    void testMetricsServiceRegistraCounterLogins() {
        metricsService.incrementarLoginFalho();

        Counter counter = meterRegistry.find("vemresolver.logins.falhos").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count());
    }

    @Test
    void testCountersIncrementamIndependentemente() {
        metricsService.incrementarPropostaEnviada();
        metricsService.incrementarContratoFinalizado();
        metricsService.incrementarLoginFalho();
        metricsService.incrementarLoginFalho();

        Counter propostas = meterRegistry.find("vemresolver.propostas.enviadas").counter();
        Counter contratos = meterRegistry.find("vemresolver.contratos.finalizados").counter();
        Counter logins = meterRegistry.find("vemresolver.logins.falhos").counter();

        assertEquals(1.0, propostas.count());
        assertEquals(1.0, contratos.count());
        assertEquals(2.0, logins.count());
    }
}
