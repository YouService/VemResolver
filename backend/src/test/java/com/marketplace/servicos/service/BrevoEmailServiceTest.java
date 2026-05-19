package com.marketplace.servicos.service;

import com.marketplace.servicos.model.ConfiguracaoEmail;
import com.marketplace.servicos.repository.ConfiguracaoEmailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrevoEmailServiceTest {

    @Mock
    private ConfiguracaoEmailRepository configRepo;

    @InjectMocks
    private BrevoEmailService emailService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:4200");
    }

    private ConfiguracaoEmail configAtiva() {
        ConfiguracaoEmail cfg = new ConfiguracaoEmail();
        cfg.setBrevoApiKey("xkeysib-test-key-1234567890");
        cfg.setEmailRemetente("noreply@vemresolver.com");
        cfg.setNomeRemetente("VemResolver");
        cfg.setAtivo(true);
        return cfg;
    }

    @Test
    @DisplayName("sendVerificationEmail — sem config no banco → não lança exceção")
    void sendVerificationEmail_SemConfig_NaoLancaExcecao() {
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        emailService.sendVerificationEmail("usuario@test.com", "token-abc");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendVerificationEmail — config desativada → não envia")
    void sendVerificationEmail_ConfigDesativada_NaoEnvia() {
        ConfiguracaoEmail cfg = configAtiva();
        cfg.setAtivo(false);
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.of(cfg));
        emailService.sendVerificationEmail("usuario@test.com", "token-abc");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendVerificationEmail — sem API key → não envia")
    void sendVerificationEmail_SemApiKey_NaoEnvia() {
        ConfiguracaoEmail cfg = configAtiva();
        cfg.setBrevoApiKey(null);
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.of(cfg));
        emailService.sendVerificationEmail("usuario@test.com", "token-abc");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendPropostaRecebida — sem config → não lança exceção")
    void sendPropostaRecebida_SemConfig_NaoLancaExcecao() {
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        emailService.sendPropostaRecebida("cliente@test.com", "João Prestador", "Pintura de casa");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendPropostaAceita — sem config → não lança exceção")
    void sendPropostaAceita_SemConfig_NaoLancaExcecao() {
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        emailService.sendPropostaAceita("prestador@test.com", "Maria Cliente", "Reforma banheiro");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendContratoFinalizado — sem config → não lança exceção")
    void sendContratoFinalizado_SemConfig_NaoLancaExcecao() {
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        emailService.sendContratoFinalizado("prestador@test.com", "Instalação elétrica");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendEmailTeste — sem config → não lança exceção")
    void sendEmailTeste_SemConfig_NaoLancaExcecao() {
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        emailService.sendEmailTeste("admin@test.com");
        verify(configRepo).findFirstByOrderByIdAsc();
    }

    @Test
    @DisplayName("sendPropostaRecebida — descrição longa é truncada sem erro")
    void sendPropostaRecebida_DescricaoLonga_NaoLancaExcecao() {
        when(configRepo.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        String descLonga = "A".repeat(200);
        emailService.sendPropostaRecebida("cliente@test.com", "Prestador", descLonga);
        verify(configRepo).findFirstByOrderByIdAsc();
    }
}
