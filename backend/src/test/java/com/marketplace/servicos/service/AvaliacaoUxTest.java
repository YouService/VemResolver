package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AvaliacaoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class AvaliacaoUxTest {

    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private SolicitacaoServicoRepository solicitacaoRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private UserRepository userRepository;
    @Mock private PropostaRepository propostaRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private PrestadorService prestadorService;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    private Avaliacao buildAvaliacao(Long id, Long clienteId, Long prestadorUserId, TipoAvaliacao tipo) {
        User cliente = new User();
        cliente.setId(clienteId);
        cliente.setNome("Cliente");
        cliente.setEmail("c@test.com");
        cliente.setAvaliacaoMedia(0.0);
        cliente.setTotalAvaliacoes(0);

        User prestadorUser = new User();
        prestadorUser.setId(prestadorUserId);
        prestadorUser.setNome("Prestador");
        prestadorUser.setEmail("p@test.com");
        prestadorUser.setAvaliacaoMedia(0.0);
        prestadorUser.setTotalAvaliacoes(0);

        Prestador prestador = new Prestador();
        prestador.setId(prestadorUserId + 100L);
        prestador.setUsuario(prestadorUser);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);

        SolicitacaoServico solicitacao = new SolicitacaoServico();
        solicitacao.setId(10L);
        solicitacao.setCliente(cliente);
        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);

        Avaliacao a = new Avaliacao();
        a.setId(id);
        a.setCliente(cliente);
        a.setPrestador(prestador);
        a.setSolicitacao(solicitacao);
        a.setTipo(tipo);
        a.setNota(5);
        a.setComentario("Excelente!");
        a.setDataCriacao(LocalDateTime.now());
        return a;
    }

    // ── P10.3 — Filtro por Nota ───────────────────────────────────────────────

    @Test
    @DisplayName("AU-01: listarPorPrestador sem filtro de nota chama filtrarPorNota com nota=null")
    void listarPorPrestador_SemFiltro_UsaFiltrarPorNota() {
        Pageable pg = PageRequest.of(0, 10);
        when(avaliacaoRepository.filtrarPorNota(eq(1L), isNull(), eq(pg)))
                .thenReturn(Page.empty(pg));

        avaliacaoService.listarPorPrestador(1L, null, pg);

        verify(avaliacaoRepository).filtrarPorNota(1L, null, pg);
    }

    @Test
    @DisplayName("AU-02: listarPorPrestador com nota=5 filtra apenas avaliações 5 estrelas")
    void listarPorPrestador_ComFiltroNota5_FiltraCorretamente() {
        Pageable pg = PageRequest.of(0, 10);
        Avaliacao av = buildAvaliacao(1L, 10L, 20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);
        av.setNota(5);
        when(avaliacaoRepository.filtrarPorNota(eq(1L), eq(5), eq(pg)))
                .thenReturn(new PageImpl<>(List.of(av), pg, 1));

        Page<AvaliacaoDTO> resultado = avaliacaoService.listarPorPrestador(1L, 5, pg);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(5, resultado.getContent().get(0).getNota());
    }

    // ── P10.4 — Resposta a Avaliações ────────────────────────────────────────

    @Test
    @DisplayName("AU-03: prestador responde avaliação com sucesso")
    void responderAvaliacao_PrestadorDono_Sucesso() {
        Long avaliacaoId = 1L;
        Long prestadorUserId = 20L;
        Avaliacao av = buildAvaliacao(avaliacaoId, 10L, prestadorUserId, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(av));
        when(avaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoDTO dto = avaliacaoService.responderAvaliacao(avaliacaoId, "Obrigado pelo feedback!", prestadorUserId);

        assertEquals("Obrigado pelo feedback!", dto.getRespostaPrestador());
        assertNotNull(dto.getDataResposta());
    }

    @Test
    @DisplayName("AU-04: outro prestador tenta responder avaliação que não é dele → BusinessException")
    void responderAvaliacao_OutroPrestador_LancaBusinessException() {
        Long avaliacaoId = 1L;
        Long outroPrestadorId = 99L;
        Avaliacao av = buildAvaliacao(avaliacaoId, 10L, 20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(av));

        assertThrows(BusinessException.class,
                () -> avaliacaoService.responderAvaliacao(avaliacaoId, "Resposta", outroPrestadorId));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("AU-05: resposta vazia lança BusinessException")
    void responderAvaliacao_RespostaVazia_LancaBusinessException() {
        Long avaliacaoId = 1L;
        Long prestadorUserId = 20L;
        Avaliacao av = buildAvaliacao(avaliacaoId, 10L, prestadorUserId, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(av));

        assertThrows(BusinessException.class,
                () -> avaliacaoService.responderAvaliacao(avaliacaoId, "   ", prestadorUserId));
    }

    // ── P10.4 — Edição pelo Cliente ──────────────────────────────────────────

    @Test
    @DisplayName("AU-06: cliente edita avaliação dentro de 7 dias com sucesso")
    void editarAvaliacao_ClienteDonoDentro7Dias_Sucesso() {
        Long avaliacaoId = 1L;
        Long clienteId = 10L;
        Avaliacao av = buildAvaliacao(avaliacaoId, clienteId, 20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);
        av.setDataCriacao(LocalDateTime.now().minusDays(2));

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(av));
        when(avaliacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(avaliacaoRepository.findByPrestadorIdAndTipo(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(av), PageRequest.of(0, 10), 1));
        doNothing().when(prestadorService).recalcularVerificado(any());
        when(prestadorRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoDTO dto = avaliacaoService.editarAvaliacao(avaliacaoId, 4, "Bom serviço", clienteId);

        assertEquals(4, dto.getNota());
        assertEquals("Bom serviço", dto.getComentario());
    }

    @Test
    @DisplayName("AU-07: cliente tenta editar avaliação com mais de 7 dias → BusinessException")
    void editarAvaliacao_AposSeteDias_LancaBusinessException() {
        Long avaliacaoId = 1L;
        Long clienteId = 10L;
        Avaliacao av = buildAvaliacao(avaliacaoId, clienteId, 20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);
        av.setDataCriacao(LocalDateTime.now().minusDays(8));

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(av));

        assertThrows(BusinessException.class,
                () -> avaliacaoService.editarAvaliacao(avaliacaoId, 3, "Mudei de ideia", clienteId));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("AU-08: outro cliente tenta editar avaliação que não é dele → BusinessException")
    void editarAvaliacao_OutroCliente_LancaBusinessException() {
        Long avaliacaoId = 1L;
        Long outroCliente = 99L;
        Avaliacao av = buildAvaliacao(avaliacaoId, 10L, 20L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(av));

        assertThrows(BusinessException.class,
                () -> avaliacaoService.editarAvaliacao(avaliacaoId, 2, "...", outroCliente));
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("AU-09: avaliação inexistente → ResourceNotFoundException")
    void editarOuResponder_AvaliacaoInexistente_LancaResourceNotFoundException() {
        when(avaliacaoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> avaliacaoService.responderAvaliacao(99L, "texto", 1L));
        assertThrows(ResourceNotFoundException.class,
                () -> avaliacaoService.editarAvaliacao(99L, 5, "texto", 1L));
    }
}
