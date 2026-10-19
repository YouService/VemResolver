package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PropostaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

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

    private User cliente;
    private User prestadorUser;
    private Prestador prestador;
    private SolicitacaoServico solicitacao;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Inject @Lazy field not handled by constructor injection
        org.springframework.test.util.ReflectionTestUtils.setField(propostaService, "webPushService", webPushService);

        cliente = new User();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@cliente.com");

        prestadorUser = new User();
        prestadorUser.setId(2L);
        prestadorUser.setNome("Maria");

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Elétrica");

        prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(prestadorUser);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);

        solicitacao = new SolicitacaoServico();
        solicitacao.setId(20L);
        solicitacao.setCliente(cliente);
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricao("Troca de fiação");
        solicitacao.setCidade("São Paulo");
        solicitacao.setEstado("SP");
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
    }

    // ── criar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criar: solicitação aberta e prestador sem proposta anterior — persiste e notifica")
    void criar_SolicitacaoAberta_PersisteProposta() {
        PropostaDTO dto = new PropostaDTO();
        dto.setSolicitacaoId(20L);
        dto.setValor(BigDecimal.valueOf(300.0));
        dto.setMensagem("Faço com qualidade");

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(propostaRepository.existsBySolicitacaoIdAndPrestadorId(20L, 10L)).thenReturn(false);
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(any(), any())).thenReturn(false);
        when(propostaRepository.save(any(Proposta.class))).thenAnswer(inv -> {
            Proposta p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        PropostaDTO resultado = propostaService.criar(dto, 2L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        verify(propostaRepository).save(any(Proposta.class));
        verify(notificacaoService).criar(eq(1L), any(), eq("NOVA_PROPOSTA"), any());
    }

    @Test
    @DisplayName("criar: solicitação não está ABERTA lança BusinessException")
    void criar_SolicitacaoNaoAberta_LancaException() {
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        PropostaDTO dto = new PropostaDTO();
        dto.setSolicitacaoId(20L);
        dto.setValor(BigDecimal.valueOf(100.0));

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> propostaService.criar(dto, 2L));
        assertTrue(ex.getMessage().contains("aberta"));
        verify(propostaRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: prestador já enviou proposta para a mesma solicitação — lança BusinessException")
    void criar_PropostaJaEnviada_LancaException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setSolicitacaoId(20L);
        dto.setValor(BigDecimal.valueOf(200.0));

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(propostaRepository.existsBySolicitacaoIdAndPrestadorId(20L, 10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> propostaService.criar(dto, 2L));
        assertTrue(ex.getMessage().contains("já enviou"));
        verify(propostaRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: prestador sem créditos disponíveis — lança BusinessException e não persiste")
    void criar_PrestadorSemCreditos_LancaException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setSolicitacaoId(20L);
        dto.setValor(BigDecimal.valueOf(200.0));

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(propostaRepository.existsBySolicitacaoIdAndPrestadorId(20L, 10L)).thenReturn(false);
        doThrow(new BusinessException("Você não tem créditos disponíveis"))
                .when(creditoService).validarCreditoParaProposta(prestador);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> propostaService.criar(dto, 2L));
        assertTrue(ex.getMessage().contains("créditos"));
        verify(propostaRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: prestador não cadastrado lança ResourceNotFoundException")
    void criar_PrestadorNaoCadastrado_LancaException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setSolicitacaoId(20L);
        dto.setValor(BigDecimal.valueOf(100.0));

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> propostaService.criar(dto, 99L));
    }

    // ── aceitarProposta ──────────────────────────────────────────────────────

    @Test
    @DisplayName("aceitarProposta: cliente dono aceita proposta ENVIADA — status muda para ACEITA")
    void aceitarProposta_DonoDaSolicitacao_RetornaAceita() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(any(), any())).thenReturn(false);
        when(propostaRepository.save(any())).thenReturn(proposta);
        when(solicitacaoRepository.save(any())).thenReturn(solicitacao);

        PropostaDTO dto = propostaService.aceitarProposta(100L, 1L);

        assertEquals(StatusProposta.ACEITA, dto.getStatus());
        assertEquals(StatusSolicitacao.EM_ANDAMENTO, proposta.getSolicitacao().getStatus());
        verify(notificacaoService).criar(eq(2L), any(), eq("PROPOSTA_ACEITA"), any());
    }

    @Test
    @DisplayName("aceitarProposta: usuário que não é o cliente dono lança BusinessException")
    void aceitarProposta_NaoDono_LancaException() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> propostaService.aceitarProposta(100L, 999L));
        assertTrue(ex.getMessage().contains("cliente dono"));
        verify(propostaRepository, never()).save(any());
    }

    @Test
    @DisplayName("aceitarProposta: proposta já processada (ACEITA) lança BusinessException")
    void aceitarProposta_JaProcessada_LancaException() {
        Proposta proposta = buildProposta(StatusProposta.ACEITA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> propostaService.aceitarProposta(100L, 1L));
        assertTrue(ex.getMessage().contains("processada"));
    }

    @Test
    @DisplayName("aceitarProposta: debita crédito do prestador antes de aceitar")
    void aceitarProposta_DebitaCreditoDoPrestador() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(any(), any())).thenReturn(false);
        when(propostaRepository.save(any())).thenReturn(proposta);

        propostaService.aceitarProposta(100L, 1L);

        verify(creditoService).consumirPorAceite(proposta);
    }

    @Test
    @DisplayName("aceitarProposta: prestador sem créditos — proposta continua ENVIADA")
    void aceitarProposta_PrestadorSemCreditos_LancaException() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));
        doThrow(new BusinessException("Este prestador está sem créditos no momento"))
                .when(creditoService).consumirPorAceite(proposta);

        assertThrows(BusinessException.class, () -> propostaService.aceitarProposta(100L, 1L));
        assertEquals(StatusProposta.ENVIADA, proposta.getStatus());
        verify(propostaRepository, never()).save(any());
    }

    // ── recusarProposta ──────────────────────────────────────────────────────

    @Test
    @DisplayName("recusarProposta: cliente dono recusa proposta ENVIADA — status muda para RECUSADA")
    void recusarProposta_DonoDaSolicitacao_RetornaRecusada() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(any(), any())).thenReturn(false);
        when(propostaRepository.save(any())).thenReturn(proposta);

        PropostaDTO dto = propostaService.recusarProposta(100L, 1L);

        assertEquals(StatusProposta.RECUSADA, dto.getStatus());
    }

    @Test
    @DisplayName("recusarProposta: usuário que não é o cliente dono lança BusinessException")
    void recusarProposta_NaoDono_LancaException() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);

        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> propostaService.recusarProposta(100L, 999L));
    }

    // ── buscarPorId ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: participante da proposta (cliente) tem acesso")
    void buscarPorId_ParticipanteCliente_RetornaDTO() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);
        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(any(), any())).thenReturn(false);

        PropostaDTO dto = propostaService.buscarPorId(100L, 1L);

        assertNotNull(dto);
    }

    @Test
    @DisplayName("buscarPorId: terceiro sem vínculo com a proposta lança BusinessException")
    void buscarPorId_UsuarioForaDaProposta_LancaException() {
        Proposta proposta = buildProposta(StatusProposta.ENVIADA);
        when(propostaRepository.findById(100L)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> propostaService.buscarPorId(100L, 999L));
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Proposta buildProposta(StatusProposta status) {
        Proposta p = new Proposta();
        p.setId(100L);
        p.setSolicitacao(solicitacao);
        p.setPrestador(prestador);
        p.setValor(BigDecimal.valueOf(250.0));
        p.setMensagem("Proposta");
        p.setStatus(status);
        return p;
    }
}
