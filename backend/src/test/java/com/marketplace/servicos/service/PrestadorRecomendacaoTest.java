package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PrestadorRecomendadoDTO;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PrestadorRecomendacaoTest {

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private SolicitacaoServicoRepository solicitacaoRepository;

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private FotoPrestadorRepository fotoPrestadorRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @InjectMocks
    private PrestadorService prestadorService;

    private SolicitacaoServico solicitacao;
    private Prestador prestador1;
    private Prestador prestador2;
    private Categoria categoria;
    private User usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Encanador");

        usuario = new User();
        usuario.setId(1L);
        usuario.setNome("João");
        usuario.setAtivo(true);

        User usuarioPrestador1 = new User();
        usuarioPrestador1.setId(2L);
        usuarioPrestador1.setNome("Prestador Um");
        usuarioPrestador1.setAtivo(true);

        User usuarioPrestador2 = new User();
        usuarioPrestador2.setId(3L);
        usuarioPrestador2.setNome("Prestador Dois");
        usuarioPrestador2.setAtivo(true);

        prestador1 = new Prestador();
        prestador1.setId(1L);
        prestador1.setUsuario(usuarioPrestador1);
        prestador1.setCategorias(List.of(categoria));
        prestador1.setAvaliacaoMedia(4.8);
        prestador1.setTotalAvaliacoes(10);
        prestador1.setLatitude(-23.5505);
        prestador1.setLongitude(-46.6333);
        prestador1.setPercentualCompletude(100);

        prestador2 = new Prestador();
        prestador2.setId(2L);
        prestador2.setUsuario(usuarioPrestador2);
        prestador2.setCategorias(List.of(categoria));
        prestador2.setAvaliacaoMedia(4.2);
        prestador2.setTotalAvaliacoes(5);
        prestador2.setLatitude(null);
        prestador2.setLongitude(null);
        prestador2.setPercentualCompletude(75);

        solicitacao = new SolicitacaoServico();
        solicitacao.setId(1L);
        solicitacao.setCliente(usuario);
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricao("Reparo de encanamento");
        solicitacao.setCidade("São Paulo");
        solicitacao.setEstado("SP");
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
    }

    @Test
    void testScoreCorrotoComAvaliacao() {
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findAtivosParaRecomendacao(1L)).thenReturn(List.of(prestador1));
        when(propostaRepository.countByPrestadorIdAndStatus(1L, StatusProposta.ACEITA)).thenReturn(8L);

        List<PrestadorRecomendadoDTO> recomendados = prestadorService.buscarRecomendados(1L, 5);

        assertNotNull(recomendados);
        assertFalse(recomendados.isEmpty());
        PrestadorRecomendadoDTO result = recomendados.get(0);
        assertEquals(prestador1.getId(), result.getId());
        assertTrue(result.getScore() > 0);
        assertTrue(result.getScore() <= 10);
    }

    @Test
    void testScoreSemCoordenadas() {
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findAtivosParaRecomendacao(1L)).thenReturn(List.of(prestador2));
        when(propostaRepository.countByPrestadorIdAndStatus(2L, StatusProposta.ACEITA)).thenReturn(3L);

        List<PrestadorRecomendadoDTO> recomendados = prestadorService.buscarRecomendados(1L, 5);

        assertNotNull(recomendados);
        assertFalse(recomendados.isEmpty());
        PrestadorRecomendadoDTO result = recomendados.get(0);
        assertEquals(prestador2.getId(), result.getId());
        assertTrue(result.getScore() > 0);
    }

    @Test
    void testLimiteRespeitado() {
        List<Prestador> prestadores = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Prestador p = new Prestador();
            p.setId((long) i);
            User u = new User();
            u.setId((long) (100 + i));
            u.setAtivo(true);
            p.setUsuario(u);
            p.setCategorias(List.of(categoria));
            p.setAvaliacaoMedia(4.5 - (i * 0.1));
            p.setPercentualCompletude(80);
            prestadores.add(p);
        }

        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findAtivosParaRecomendacao(1L)).thenReturn(prestadores);
        when(propostaRepository.countByPrestadorIdAndStatus(any(), any())).thenReturn(5L);

        List<PrestadorRecomendadoDTO> recomendados = prestadorService.buscarRecomendados(1L, 3);

        assertEquals(3, recomendados.size());
    }

    @Test
    void testListaVaziaQuandoNenhomPrestadorNaCategoria() {
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findAtivosParaRecomendacao(1L)).thenReturn(List.of());

        List<PrestadorRecomendadoDTO> recomendados = prestadorService.buscarRecomendados(1L, 5);

        assertNotNull(recomendados);
        assertTrue(recomendados.isEmpty());
    }

    @Test
    void testOrdenacaoDescendente() {
        Prestador prestador3 = new Prestador();
        prestador3.setId(3L);
        User u3 = new User();
        u3.setId(4L);
        u3.setAtivo(true);
        prestador3.setUsuario(u3);
        prestador3.setCategorias(List.of(categoria));
        prestador3.setAvaliacaoMedia(3.5);
        prestador3.setPercentualCompletude(50);

        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findAtivosParaRecomendacao(1L))
            .thenReturn(List.of(prestador1, prestador2, prestador3));
        when(propostaRepository.countByPrestadorIdAndStatus(any(), any())).thenReturn(5L);

        List<PrestadorRecomendadoDTO> recomendados = prestadorService.buscarRecomendados(1L, 5);

        assertFalse(recomendados.isEmpty());
        for (int i = 0; i < recomendados.size() - 1; i++) {
            assertTrue(recomendados.get(i).getScore() >= recomendados.get(i + 1).getScore());
        }
    }

    @Test
    void testJustificativaFormatada() {
        when(solicitacaoRepository.findById(1L)).thenReturn(Optional.of(solicitacao));
        when(prestadorRepository.findAtivosParaRecomendacao(1L)).thenReturn(List.of(prestador1));
        when(propostaRepository.countByPrestadorIdAndStatus(1L, StatusProposta.ACEITA)).thenReturn(8L);

        List<PrestadorRecomendadoDTO> recomendados = prestadorService.buscarRecomendados(1L, 5);

        assertNotNull(recomendados);
        assertFalse(recomendados.isEmpty());
        PrestadorRecomendadoDTO result = recomendados.get(0);
        assertNotNull(result.getJustificativa());
        assertTrue(result.getJustificativa().contains("4.") || result.getJustificativa().contains("contratos"));
        assertTrue(result.getJustificativa().contains("perfil"));
    }
}
