package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PropostaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropostaCancelamentoTest {

    @Mock private PropostaRepository propostaRepository;
    @Mock private SolicitacaoServicoRepository solicitacaoRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private EmailService emailService;
    @Mock private WebPushService webPushService;
    @Mock private CreditoService creditoService;

    @InjectMocks
    private PropostaService propostaService;

    private Proposta buildProposta(Long propostaId, Long clienteId, Long prestadorUserId, StatusProposta status) {
        User clienteUser = new User();
        clienteUser.setId(clienteId);
        clienteUser.setNome("Cliente");
        clienteUser.setEmail("cliente@test.com");
        clienteUser.setAvaliacaoMedia(0.0);
        clienteUser.setTotalAvaliacoes(0);

        User prestadorUser = new User();
        prestadorUser.setId(prestadorUserId);
        prestadorUser.setNome("Prestador");
        prestadorUser.setEmail("prestador@test.com");
        prestadorUser.setAvaliacaoMedia(0.0);
        prestadorUser.setTotalAvaliacoes(0);

        Prestador prestador = new Prestador();
        prestador.setId(prestadorUserId + 100L);
        prestador.setUsuario(prestadorUser);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);

        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNome("Elétrica");

        SolicitacaoServico solicitacao = new SolicitacaoServico();
        solicitacao.setId(10L);
        solicitacao.setCliente(clienteUser);
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
        solicitacao.setDescricao("Instalar tomada");
        solicitacao.setCidade("SP");
        solicitacao.setEstado("SP");
        solicitacao.setCategoria(cat);

        Proposta p = new Proposta();
        p.setId(propostaId);
        p.setPrestador(prestador);
        p.setSolicitacao(solicitacao);
        p.setStatus(status);
        p.setValor(java.math.BigDecimal.valueOf(150));
        return p;
    }

    // PC-01: prestador cancela proposta ENVIADA com sucesso
    @Test
    @DisplayName("PC-01: prestador cancela proposta ENVIADA → status CANCELADA_PELO_PRESTADOR")
    void cancelar_PropostaEnviada_AlteraStatusParaCancelada() {
        Long propostaId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, 5L, prestadorUserId, StatusProposta.ENVIADA);
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(propostaRepository.save(any(Proposta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(any(), any())).thenReturn(false);

        PropostaDTO result = propostaService.cancelarProposta(propostaId, prestadorUserId);

        assertNotNull(result);
        assertEquals(StatusProposta.CANCELADA_PELO_PRESTADOR, result.getStatus());
        verify(propostaRepository).save(proposta);
    }

    // PC-02: cliente não pode cancelar proposta (só o prestador)
    @Test
    @DisplayName("PC-02: usuário que não é o prestador tenta cancelar → BusinessException")
    void cancelar_UsuarioNaoEhPrestador_LancaBusinessException() {
        Long propostaId = 1L;
        Long outroUsuarioId = 99L;

        Proposta proposta = buildProposta(propostaId, 5L, 2L, StatusProposta.ENVIADA);
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> propostaService.cancelarProposta(propostaId, outroUsuarioId));
        verify(propostaRepository, never()).save(any());
    }

    // PC-03: proposta ACEITA não pode ser cancelada
    @Test
    @DisplayName("PC-03: tentar cancelar proposta ACEITA → BusinessException")
    void cancelar_PropostaAceita_LancaBusinessException() {
        Long propostaId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, 5L, prestadorUserId, StatusProposta.ACEITA);
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> propostaService.cancelarProposta(propostaId, prestadorUserId));
        verify(propostaRepository, never()).save(any());
    }

    // PC-04: proposta RECUSADA não pode ser cancelada
    @Test
    @DisplayName("PC-04: tentar cancelar proposta RECUSADA → BusinessException")
    void cancelar_PropostaRecusada_LancaBusinessException() {
        Long propostaId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, 5L, prestadorUserId, StatusProposta.RECUSADA);
        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> propostaService.cancelarProposta(propostaId, prestadorUserId));
        verify(propostaRepository, never()).save(any());
    }

    // PC-05: proposta inexistente → ResourceNotFoundException
    @Test
    @DisplayName("PC-05: proposta inexistente → ResourceNotFoundException")
    void cancelar_PropostaInexistente_LancaResourceNotFoundException() {
        when(propostaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propostaService.cancelarProposta(99L, 1L));
    }

    // PC-06: StatusProposta.CANCELADA_PELO_PRESTADOR existe no enum
    @Test
    @DisplayName("PC-06: enum StatusProposta contém CANCELADA_PELO_PRESTADOR")
    void statusProposta_ContemCanceladaPeloPrestador() {
        StatusProposta status = StatusProposta.CANCELADA_PELO_PRESTADOR;
        assertEquals("CANCELADA_PELO_PRESTADOR", status.name());
    }
}
