package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AvaliacaoDTO;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private SolicitacaoServicoRepository solicitacaoRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropostaRepository propostaRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private PrestadorService prestadorService;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    private User cliente;
    private User prestadorUser;
    private Prestador prestador;
    private SolicitacaoServico solicitacao;

    @BeforeEach
    void setUp() {
        cliente = new User();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@cliente.com");
        cliente.setAvaliacaoMedia(0.0);
        cliente.setTotalAvaliacoes(0);

        prestadorUser = new User();
        prestadorUser.setId(2L);
        prestadorUser.setNome("Maria");

        prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(prestadorUser);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);

        solicitacao = new SolicitacaoServico();
        solicitacao.setId(20L);
        solicitacao.setCliente(cliente);
        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
        solicitacao.setDescricao("Serviço concluído");
        solicitacao.setCidade("SP");
        solicitacao.setEstado("SP");
    }

    // ── criar (cliente avalia prestador) ─────────────────────────────────────

    @Test
    @DisplayName("criar: cliente avalia prestador em solicitação concluída — persiste e notifica")
    void criar_SolicitacaoConcluida_PersistiAvaliacao() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(5, "Excelente");

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(false);
        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        stubMediaPrestador(List.of(5));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> {
            Avaliacao a = inv.getArgument(0);
            a.setId(50L);
            return a;
        });

        AvaliacaoDTO resultado = avaliacaoService.criar(dto, 1L);

        assertNotNull(resultado);
        assertEquals(TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR, resultado.getTipo());
        assertEquals(5, resultado.getNota());
        verify(notificacaoService).criar(eq(2L), any(), eq("AVALIACAO_RECEBIDA"), any());
    }

    @Test
    @DisplayName("criar: solicitação não concluída lança BusinessException")
    void criar_SolicitacaoNaoConcluida_LancaException() {
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        AvaliacaoDTO dto = buildAvaliacaoDTO(4, null);

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> avaliacaoService.criar(dto, 1L));
        assertTrue(ex.getMessage().contains("concluídas"));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: cliente que não é dono da solicitação lança BusinessException")
    void criar_ClienteNaoDono_LancaException() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(3, null);

        // ownership check (clienteId=999 != 1) é anterior ao duplicate check — sem stub extra
        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> avaliacaoService.criar(dto, 999L));
        assertTrue(ex.getMessage().contains("não pode avaliar"));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: avaliação duplicada lança BusinessException")
    void criar_AvaliacaoDuplicada_LancaException() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(4, null);

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> avaliacaoService.criar(dto, 1L));
        assertTrue(ex.getMessage().contains("já foi avaliada"));
        verify(avaliacaoRepository, never()).save(any());
    }

    // ── criarPorPrestador ────────────────────────────────────────────────────

    @Test
    @DisplayName("criarPorPrestador: prestador que prestou o serviço avalia cliente — persiste e notifica")
    void criarPorPrestador_PrestadorPrestouServico_PersistAvaliacao() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(4, "Bom cliente");

        Proposta propostaAceita = new Proposta();
        propostaAceita.setStatus(StatusProposta.ACEITA);

        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(20L, TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE))
                .thenReturn(false);
        when(propostaRepository.findBySolicitacaoIdAndPrestadorId(20L, 10L))
                .thenReturn(Optional.of(propostaAceita));
        stubMediaCliente(List.of(4));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> {
            Avaliacao a = inv.getArgument(0);
            a.setId(51L);
            return a;
        });

        AvaliacaoDTO resultado = avaliacaoService.criarPorPrestador(dto, 2L);

        assertNotNull(resultado);
        assertEquals(TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE, resultado.getTipo());
        verify(notificacaoService).criar(eq(1L), any(), eq("AVALIACAO_RECEBIDA"), any());
    }

    @Test
    @DisplayName("criarPorPrestador: prestador que não participou do serviço lança BusinessException")
    void criarPorPrestador_PrestadorNaoParticipou_LancaException() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(3, null);

        // proposta check precede duplicate check — sem stub para existsBySolicitacaoIdAndTipo
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(propostaRepository.findBySolicitacaoIdAndPrestadorId(20L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> avaliacaoService.criarPorPrestador(dto, 2L));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("criarPorPrestador: avaliação já registrada lança BusinessException")
    void criarPorPrestador_AvaliacaoDuplicada_LancaException() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(5, null);

        Proposta propostaAceita = new Proposta();
        propostaAceita.setStatus(StatusProposta.ACEITA);

        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(propostaRepository.findBySolicitacaoIdAndPrestadorId(20L, 10L))
                .thenReturn(Optional.of(propostaAceita));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(20L, TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> avaliacaoService.criarPorPrestador(dto, 2L));
        assertTrue(ex.getMessage().contains("já avaliou"));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("criarPorPrestador: solicitação não concluída lança BusinessException")
    void criarPorPrestador_SolicitacaoNaoConcluida_LancaException() {
        solicitacao.setStatus(StatusSolicitacao.EM_ANDAMENTO);
        AvaliacaoDTO dto = buildAvaliacaoDTO(4, null);

        // status check precede proposta e duplicate checks
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));

        assertThrows(BusinessException.class,
                () -> avaliacaoService.criarPorPrestador(dto, 2L));
    }

    // ── notas dimensionais (P4.7 Tarefa 1) ──────────────────────────────────

    @Test
    @DisplayName("Avaliacao sem dimensões: notaPontualidade, notaQualidade e notaComunicacao são null")
    void avaliacao_SemDimensoes_CamposSaoNull() {
        Avaliacao a = new Avaliacao();
        a.setNota(4);

        assertNull(a.getNotaPontualidade());
        assertNull(a.getNotaQualidade());
        assertNull(a.getNotaComunicacao());
    }

    @Test
    @DisplayName("Avaliacao com dimensões: campos armazenam os valores atribuídos")
    void avaliacao_ComDimensoes_CamposArmazenamValores() {
        Avaliacao a = new Avaliacao();
        a.setNota(5);
        a.setNotaPontualidade(5);
        a.setNotaQualidade(4);
        a.setNotaComunicacao(3);

        assertEquals(5, a.getNotaPontualidade());
        assertEquals(4, a.getNotaQualidade());
        assertEquals(3, a.getNotaComunicacao());
    }

    @Test
    @DisplayName("criar: avaliação sem dimensões persiste sem erros (campos nullable)")
    void criar_SemDimensoes_PersisteSemErro() {
        AvaliacaoDTO dto = buildAvaliacaoDTO(4, "Bom serviço");

        when(solicitacaoRepository.findById(20L)).thenReturn(Optional.of(solicitacao));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(false);
        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));
        when(userRepository.findById(1L)).thenReturn(Optional.of(cliente));
        stubMediaPrestador(List.of(4));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> {
            Avaliacao a = inv.getArgument(0);
            a.setId(55L);
            assertNull(a.getNotaPontualidade(), "notaPontualidade deve ser null quando não informada");
            assertNull(a.getNotaQualidade(),    "notaQualidade deve ser null quando não informada");
            assertNull(a.getNotaComunicacao(),  "notaComunicacao deve ser null quando não informada");
            return a;
        });

        AvaliacaoDTO resultado = avaliacaoService.criar(dto, 1L);

        assertNotNull(resultado);
        assertEquals(4, resultado.getNota());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private AvaliacaoDTO buildAvaliacaoDTO(int nota, String comentario) {
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.setSolicitacaoId(20L);
        dto.setPrestadorId(10L);
        dto.setNota(nota);
        dto.setComentario(comentario);
        return dto;
    }

    private void stubMediaPrestador(List<Integer> notas) {
        List<Avaliacao> avaliacoes = notas.stream().map(n -> {
            Avaliacao a = new Avaliacao();
            a.setNota(n);
            a.setCliente(cliente);
            a.setPrestador(prestador);
            a.setTipo(TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);
            return a;
        }).toList();
        when(avaliacaoRepository.findByPrestadorIdAndTipo(eq(10L),
                eq(TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR), any(Pageable.class)))
                .thenReturn(new PageImpl<>(avaliacoes));
        when(prestadorRepository.save(any())).thenReturn(prestador);
    }

    private void stubMediaCliente(List<Integer> notas) {
        List<Avaliacao> avaliacoes = notas.stream().map(n -> {
            Avaliacao a = new Avaliacao();
            a.setNota(n);
            a.setCliente(cliente);
            a.setPrestador(prestador);
            a.setTipo(TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE);
            return a;
        }).toList();
        when(avaliacaoRepository.findByClienteIdAndTipo(eq(1L),
                eq(TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(avaliacoes));
        when(userRepository.save(any())).thenReturn(cliente);
    }
}
