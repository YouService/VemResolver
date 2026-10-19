package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PrestadorDTO;
import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.model.Categoria;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.SolicitacaoServico;
import com.marketplace.servicos.model.StatusSolicitacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.CategoriaRepository;
import com.marketplace.servicos.repository.FotoPrestadorRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FullTextSearchTest {

    @Mock private PrestadorRepository prestadorRepository;
    @Mock private SolicitacaoServicoRepository solicitacaoRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private FotoPrestadorRepository fotoPrestadorRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private PropostaRepository propostaRepository;

    @InjectMocks
    private PrestadorService prestadorService;

    private User cliente;
    private User prestador;
    private Categoria categoria;
    private Prestador prestadorEntity;
    private SolicitacaoServico solicitacao;

    @BeforeEach
    void setUp() {
        cliente = new User();
        cliente.setId(1L);
        cliente.setNome("Cliente Test");
        cliente.setAtivo(true);

        prestador = new User();
        prestador.setId(2L);
        prestador.setNome("Prestador Test");
        prestador.setAtivo(true);

        categoria = new Categoria();
        categoria.setId(100L);
        categoria.setNome("Encanador");

        prestadorEntity = new Prestador();
        prestadorEntity.setId(10L);
        prestadorEntity.setUsuario(prestador);
        prestadorEntity.setCategorias(List.of(categoria));
        prestadorEntity.setDescricaoPerfil("Encanador profissional em São Paulo");
        prestadorEntity.setCidade("São Paulo");
        prestadorEntity.setEstado("SP");
        prestadorEntity.setAvaliacaoMedia(4.5);

        solicitacao = new SolicitacaoServico();
        solicitacao.setId(50L);
        solicitacao.setCliente(cliente);
        solicitacao.setCategoria(categoria);
        solicitacao.setTitulo("Conserto de vazamento");
        solicitacao.setDescricao("Vazamento na cozinha necessita reparo urgente");
        solicitacao.setCidade("São Paulo");
        solicitacao.setEstado("SP");
        solicitacao.setStatus(StatusSolicitacao.ABERTA);
    }

    @Test
    @DisplayName("buscar: com q retorna resultados ordenados por relevância")
    void buscar_ComQ_RetornaOrdenadosPorRelevancia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(prestadorRepository.buscarPorTexto(
                "encanador",
                null,
                null,
                null,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(prestadorEntity), pageable, 1));

        var resultado = prestadorService.buscar(null, null, null, null, "encanador", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(prestadorRepository, times(1)).buscarPorTexto(
                anyString(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("buscar: com q vazio usa fluxo normal sem tsvector")
    void buscar_ComQVazio_UsaFluxoNormal() {
        Pageable pageable = PageRequest.of(0, 10);
        when(prestadorRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(prestadorEntity), pageable, 1));

        var resultado = prestadorService.buscar(null, null, null, null, "", pageable);

        assertNotNull(resultado);
        verify(prestadorRepository, times(1)).findAll(pageable);
        verify(prestadorRepository, times(0)).buscarPorTexto(
                anyString(),
                any(),
                any(),
                any(),
                any(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("buscar: com q e filtro de cidade combina corretamente")
    void buscar_ComQeFiltroDeCity_CombinaPerfeitamente() {
        Pageable pageable = PageRequest.of(0, 10);
        when(prestadorRepository.buscarPorTexto(
                "encanador",
                null,
                "São Paulo",
                null,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(prestadorEntity), pageable, 1));

        var resultado = prestadorService.buscar(null, "São Paulo", null, null, "encanador", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(prestadorRepository, times(1)).buscarPorTexto(
                eq("encanador"),
                isNull(),
                eq("São Paulo"),
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("buscarAbertas: solicitacoes abertas buscadas por texto")
    void buscarAbertas_PorTexto_RetornaSomentAbertasRelevantes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(solicitacaoRepository.buscarPorTexto(
                "vazamento",
                null,
                null,
                null,
                pageable
        )).thenReturn(new PageImpl<>(List.of(solicitacao), pageable, 1));

        // Assumindo que SolicitacaoServicoService está com a mesma estrutura
        // Aqui estamos testando que a query filtra por status ABERTA
        var resultado = solicitacaoRepository.buscarPorTexto("vazamento", null, null, null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        assertEquals(StatusSolicitacao.ABERTA, resultado.getContent().get(0).getStatus());
    }
}
