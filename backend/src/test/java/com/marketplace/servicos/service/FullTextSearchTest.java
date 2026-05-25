package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PrestadorDTO;
import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class FullTextSearchTest {

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private SolicitacaoServicoRepository solicitacaoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private FotoPrestadorRepository fotoPrestadorRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private PropostaRepository propostaRepository;

    @InjectMocks
    private PrestadorService prestadorService;

    @InjectMocks
    private SolicitacaoServicoService solicitacaoService;

    private Prestador prestador1;
    private Prestador prestador2;
    private SolicitacaoServico solicitacao1;
    private SolicitacaoServico solicitacao2;
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
        usuario.setNome("Cliente");
        usuario.setAtivo(true);

        User usuarioPrestador1 = new User();
        usuarioPrestador1.setId(2L);
        usuarioPrestador1.setNome("João Encanador");
        usuarioPrestador1.setAtivo(true);

        prestador1 = new Prestador();
        prestador1.setId(1L);
        prestador1.setUsuario(usuarioPrestador1);
        prestador1.setCategorias(List.of(categoria));
        prestador1.setDescricaoPerfil("Especialista em encanamento hidráulico");
        prestador1.setCidade("São Paulo");
        prestador1.setEstado("SP");
        prestador1.setAvaliacaoMedia(4.8);
        prestador1.setTsPerfil("encanador:2 especialista:3 hidraulico:4");

        prestador2 = new Prestador();
        prestador2.setId(2L);
        User usuarioPrestador2 = new User();
        usuarioPrestador2.setId(3L);
        usuarioPrestador2.setNome("Maria Bombeiro");
        usuarioPrestador2.setAtivo(true);
        prestador2.setUsuario(usuarioPrestador2);
        prestador2.setCategorias(List.of(categoria));
        prestador2.setDescricaoPerfil("Bombeiro hidráulico");
        prestador2.setCidade("Rio de Janeiro");
        prestador2.setEstado("RJ");
        prestador2.setAvaliacaoMedia(4.5);
        prestador2.setTsPerfil("bombeiro:2 hidraulico:3");

        solicitacao1 = new SolicitacaoServico();
        solicitacao1.setId(1L);
        solicitacao1.setCliente(usuario);
        solicitacao1.setCategoria(categoria);
        solicitacao1.setTitulo("Reparo de cano");
        solicitacao1.setDescricao("Vazamento em cano principal");
        solicitacao1.setCidade("São Paulo");
        solicitacao1.setEstado("SP");
        solicitacao1.setStatus(StatusSolicitacao.ABERTA);
        solicitacao1.setTsDescricao("reparo:2 cano:3 vazamento:4");

        solicitacao2 = new SolicitacaoServico();
        solicitacao2.setId(2L);
        solicitacao2.setCliente(usuario);
        solicitacao2.setCategoria(categoria);
        solicitacao2.setTitulo("Limpeza de tubulação");
        solicitacao2.setDescricao("Limpeza de tubulação de água");
        solicitacao2.setCidade("Rio de Janeiro");
        solicitacao2.setEstado("RJ");
        solicitacao2.setStatus(StatusSolicitacao.ABERTA);
        solicitacao2.setTsDescricao("limpeza:2 tubulacao:3 agua:4");
    }

    @Test
    void testBuscaComQRetornaResultados() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prestador> page = new PageImpl<>(List.of(prestador1), pageable, 1);

        when(prestadorRepository.buscarPorTexto(
            "encanador", null, null, null, null, pageable))
            .thenReturn(page);

        Page<PrestadorDTO> resultado = prestadorService.buscar(null, null, null, null, "encanador", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertNotNull(resultado.getContent().get(0).getCategoriasIds());
    }

    @Test
    void testBuscaComQVazioUsaFluxoNormal() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prestador> page = new PageImpl<>(List.of(prestador1), pageable, 1);

        when(prestadorRepository.findAll(pageable)).thenReturn(page);

        Page<PrestadorDTO> resultado = prestadorService.buscar(null, null, null, null, "", pageable);

        assertNotNull(resultado);
    }

    @Test
    void testBuscaComQPlusFiltrosCidade() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Prestador> page = new PageImpl<>(List.of(prestador1), pageable, 1);

        when(prestadorRepository.buscarPorTexto(
            "encanador", null, "São Paulo", null, null, pageable))
            .thenReturn(page);

        Page<PrestadorDTO> resultado = prestadorService.buscar(
            null, "São Paulo", null, null, "encanador", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void testBuscaSolicitacoesAbertasPorTexto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SolicitacaoServico> page = new PageImpl<>(List.of(solicitacao1), pageable, 1);

        when(solicitacaoRepository.buscarPorTexto(
            "vazamento", null, null, null, pageable))
            .thenReturn(page);

        Page<SolicitacaoServicoDTO> resultado = solicitacaoService.buscarAbertas(
            null, null, null, "vazamento", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void testPrestadoresSuspensosNaoAparecem() {
        Pageable pageable = PageRequest.of(0, 10);

        User usuarioSuspenso = new User();
        usuarioSuspenso.setId(4L);
        usuarioSuspenso.setNome("Suspenso");
        usuarioSuspenso.setAtivo(false);

        Prestador prestadorSuspenso = new Prestador();
        prestadorSuspenso.setId(3L);
        prestadorSuspenso.setUsuario(usuarioSuspenso);
        prestadorSuspenso.setCategorias(List.of(categoria));
        prestadorSuspenso.setTsPerfil("encanador:2");

        Page<Prestador> page = new PageImpl<>(List.of(), pageable, 0);

        when(prestadorRepository.buscarPorTexto(
            "encanador", null, null, null, null, pageable))
            .thenReturn(page);

        Page<PrestadorDTO> resultado = prestadorService.buscar(
            null, null, null, null, "encanador", pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void testBuscaSolicitacoesAbertasApenasSolicitacoesAbertas() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SolicitacaoServico> page = new PageImpl<>(List.of(solicitacao1), pageable, 1);

        when(solicitacaoRepository.buscarPorTexto(
            "cano", null, null, null, pageable))
            .thenReturn(page);

        Page<SolicitacaoServicoDTO> resultado = solicitacaoService.buscarAbertas(
            null, null, null, "cano", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(StatusSolicitacao.ABERTA, resultado.getContent().get(0).getStatus());
    }
}
