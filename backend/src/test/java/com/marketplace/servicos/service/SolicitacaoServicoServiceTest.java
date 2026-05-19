package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServicoServiceTest {

    @Mock
    private SolicitacaoServicoRepository solicitacaoRepository;

    @Mock
    private com.marketplace.servicos.repository.UserRepository userRepository;

    @Mock
    private com.marketplace.servicos.repository.CategoriaRepository categoriaRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private EmailService emailService;

    @Mock
    private PrestadorService prestadorService;

    @Mock
    private WebPushService webPushService;

    @InjectMocks
    private SolicitacaoServicoService solicitacaoService;

    private User cliente;
    private Categoria categoria;
    private SolicitacaoServico solicitacao;

    @BeforeEach
    void setUp() {
        // Inject @Lazy field not handled by constructor injection
        org.springframework.test.util.ReflectionTestUtils.setField(solicitacaoService, "webPushService", webPushService);

        cliente = new User();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@cliente.com");

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Elétrica");

        solicitacao = new SolicitacaoServico();
        solicitacao.setId(10L);
        solicitacao.setCliente(cliente);
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricao("Troca de fiação");
        solicitacao.setCidade("São Paulo");
        solicitacao.setEstado("SP");
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
    }

    @Test
    void concluirServico_EmAndamento_RetornaConcluida() {
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.of(solicitacao));
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(propostaRepository.findBySolicitacaoIdAndStatus(10L, StatusProposta.ACEITA))
                .thenReturn(Optional.empty());
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(10L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(false);
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(10L, TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE))
                .thenReturn(false);

        SolicitacaoServicoDTO resultado = solicitacaoService.concluirServico(10L, 1L);

        assertEquals(StatusSolicitacao.CONCLUIDA, resultado.getStatus());
        verify(solicitacaoRepository).save(solicitacao);
    }

    @Test
    void concluirServico_SolicitacaoNaoEncontrada_LancaException() {
        when(solicitacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> solicitacaoService.concluirServico(99L, 1L));
    }

    @Test
    void concluirServico_ClienteNaoEDono_LancaException() {
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.of(solicitacao));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> solicitacaoService.concluirServico(10L, 999L));

        assertTrue(ex.getMessage().contains("cliente responsável"));
    }

    @Test
    void concluirServico_StatusAberta_LancaException() {
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.of(solicitacao));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> solicitacaoService.concluirServico(10L, 1L));

        assertTrue(ex.getMessage().contains("em andamento"));
    }

    @Test
    void concluirServico_StatusJaConcluida_LancaException() {
        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.of(solicitacao));

        assertThrows(BusinessException.class,
                () -> solicitacaoService.concluirServico(10L, 1L));
    }

    @Test
    void buscarPorId_SolicitacaoConcluida_RetornaComPropostaAceitaId() {
        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);

        Proposta propostaAceita = new Proposta();
        propostaAceita.setId(42L);

        when(solicitacaoRepository.findById(10L)).thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(10L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(true);
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(10L, TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE))
                .thenReturn(false);
        when(propostaRepository.findBySolicitacaoIdAndStatus(10L, StatusProposta.ACEITA))
                .thenReturn(Optional.of(propostaAceita));

        SolicitacaoServicoDTO resultado = solicitacaoService.buscarPorId(10L);

        assertEquals(StatusSolicitacao.CONCLUIDA, resultado.getStatus());
        assertEquals(42L, resultado.getPropostaAceitaId());
        assertTrue(resultado.getFoiAvaliadoPeloCliente());
        assertFalse(resultado.getFoiAvaliadoPeloPrestador());
    }

    @Test
    void buscarPorId_NaoEncontrada_LancaException() {
        when(solicitacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> solicitacaoService.buscarPorId(99L));
    }
}
