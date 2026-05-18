package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.CompletudePrestadorDTO;
import com.marketplace.servicos.dto.PerfilPublicoDTO;
import com.marketplace.servicos.dto.PrestadorDTO;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestadorServiceTest {

    @Mock private PrestadorRepository prestadorRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private FotoPrestadorRepository fotoPrestadorRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private PropostaRepository propostaRepository;

    @InjectMocks
    private PrestadorService prestadorService;

    private User usuarioPrestador;
    private Categoria categoria;
    private Prestador prestador;

    @BeforeEach
    void setUp() {
        usuarioPrestador = new User();
        usuarioPrestador.setId(1L);
        usuarioPrestador.setNome("Maria");
        usuarioPrestador.setEmail("maria@prestador.com");
        usuarioPrestador.setTipo(TipoUsuario.PRESTADOR);
        usuarioPrestador.setAtivo(true);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Eletricista");

        prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(usuarioPrestador);
        prestador.setCategorias(List.of(categoria));
        prestador.setCidade("São Paulo");
        prestador.setEstado("SP");
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);
    }

    // ── criarOuAtualizar ────────────────────────────────────────────────────

    @Test
    @DisplayName("criarOuAtualizar: prestador válido cria e persiste recalculando completude")
    void criarOuAtualizar_UsuarioPrestador_CriaComSucesso() {
        PrestadorDTO dto = new PrestadorDTO();
        dto.setUsuarioId(1L);
        dto.setCategoriasIds(List.of(1L));
        dto.setDescricaoPerfil("Eletricista experiente");
        dto.setCidade("São Paulo");
        dto.setEstado("SP");

        when(userRepository.findByIdAndTipo(1L, TipoUsuario.PRESTADOR)).thenReturn(Optional.of(usuarioPrestador));
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(categoriaRepository.findAllById(List.of(1L))).thenReturn(List.of(categoria));
        when(fotoPrestadorRepository.countByPrestadorId(any())).thenReturn(0L);
        when(prestadorRepository.save(any(Prestador.class))).thenAnswer(inv -> {
            Prestador p = inv.getArgument(0);
            if (p.getId() == null) p.setId(10L);
            return p;
        });

        PrestadorDTO resultado = prestadorService.criarOuAtualizar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getUsuarioId());
        verify(prestadorRepository, times(2)).save(any(Prestador.class));
    }

    @Test
    @DisplayName("criarOuAtualizar: usuário do tipo CLIENTE lança ResourceNotFoundException")
    void criarOuAtualizar_UsuarioCliente_LancaException() {
        PrestadorDTO dto = new PrestadorDTO();
        dto.setUsuarioId(2L);
        dto.setCategoriasIds(List.of(1L));

        when(userRepository.findByIdAndTipo(2L, TipoUsuario.PRESTADOR)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> prestadorService.criarOuAtualizar(dto));
        verify(prestadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("criarOuAtualizar: categoria inexistente lança BusinessException")
    void criarOuAtualizar_CategoriaInexistente_LancaBusinessException() {
        PrestadorDTO dto = new PrestadorDTO();
        dto.setUsuarioId(1L);
        dto.setCategoriasIds(List.of(1L, 99L));

        when(userRepository.findByIdAndTipo(1L, TipoUsuario.PRESTADOR)).thenReturn(Optional.of(usuarioPrestador));
        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());
        when(categoriaRepository.findAllById(List.of(1L, 99L))).thenReturn(List.of(categoria));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> prestadorService.criarOuAtualizar(dto));
        assertTrue(ex.getMessage().contains("categorias"));
        verify(prestadorRepository, never()).save(any());
    }

    // ── buscarPorId ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorId: prestador existente retorna DTO completo")
    void buscarPorId_PrestadorExistente_RetornaDTO() {
        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));

        PrestadorDTO resultado = prestadorService.buscarPorId(10L);

        assertEquals(10L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
    }

    @Test
    @DisplayName("buscarPorId: prestador inexistente lança ResourceNotFoundException")
    void buscarPorId_PrestadorInexistente_LancaException() {
        when(prestadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> prestadorService.buscarPorId(99L));
    }

    // ── getCompletude ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCompletude: perfil vazio retorna 0%")
    void getCompletude_PerfilVazio_RetornaZeroPorcento() {
        Prestador vazio = new Prestador();
        vazio.setId(10L);
        vazio.setUsuario(usuarioPrestador);
        vazio.setCategorias(List.of());
        vazio.setAvaliacaoMedia(0.0);
        vazio.setTotalAvaliacoes(0);

        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(vazio));
        when(fotoPrestadorRepository.countByPrestadorId(10L)).thenReturn(0L);

        CompletudePrestadorDTO dto = prestadorService.getCompletude(1L);

        assertEquals(0, dto.getPercentual());
        assertEquals(7, dto.getItens().size());
        dto.getItens().forEach(i -> assertFalse(i.isConcluido()));
    }

    @Test
    @DisplayName("getCompletude: foto+descricão+categoria+localização soma 60%")
    void getCompletude_ComFotoDescricaoCategoria_Retorna60() {
        // setUp já preenche cidade/estado (+10) e categorias (+15)
        prestador.setFotoPerfilUrl("/uploads/avatar.jpg");   // +15
        prestador.setDescricaoPerfil("A".repeat(50));        // +20

        when(prestadorRepository.findByUsuarioId(1L)).thenReturn(Optional.of(prestador));
        when(fotoPrestadorRepository.countByPrestadorId(10L)).thenReturn(0L);

        CompletudePrestadorDTO dto = prestadorService.getCompletude(1L);

        assertEquals(60, dto.getPercentual());
    }

    @Test
    @DisplayName("getCompletude: usuário inexistente lança ResourceNotFoundException")
    void getCompletude_UsuarioInexistente_LancaException() {
        when(prestadorRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> prestadorService.getCompletude(99L));
    }

    // ── buscarPerfilPublico ──────────────────────────────────────────────────

    @Test
    @DisplayName("buscarPerfilPublico: prestador ativo retorna PerfilPublicoDTO com badge não verificado")
    void buscarPerfilPublico_PrestadorAtivo_RetornaDTO() {
        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));
        when(fotoPrestadorRepository.findByPrestadorIdOrderByDataUploadAsc(10L)).thenReturn(List.of());
        when(avaliacaoRepository.findTop5ByPrestadorIdAndTipoOrderByDataCriacaoDesc(10L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(List.of());
        when(propostaRepository.countByPrestadorIdAndStatusAndSolicitacaoStatus(
                10L, StatusProposta.ACEITA, StatusSolicitacao.CONCLUIDA)).thenReturn(2L);

        PerfilPublicoDTO dto = prestadorService.buscarPerfilPublico(10L);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Maria", dto.getNome());
        assertFalse(dto.isVerificado());
    }

    @Test
    @DisplayName("buscarPerfilPublico: 5+ contratos e média >= 4.0 → verificado=true")
    void buscarPerfilPublico_CriteriosVerificado_RetornaVerificado() {
        prestador.setAvaliacaoMedia(4.5);
        prestador.setTotalAvaliacoes(10);

        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));
        when(fotoPrestadorRepository.findByPrestadorIdOrderByDataUploadAsc(10L)).thenReturn(List.of());
        when(avaliacaoRepository.findTop5ByPrestadorIdAndTipoOrderByDataCriacaoDesc(10L, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR))
                .thenReturn(List.of());
        when(propostaRepository.countByPrestadorIdAndStatusAndSolicitacaoStatus(
                10L, StatusProposta.ACEITA, StatusSolicitacao.CONCLUIDA)).thenReturn(5L);

        PerfilPublicoDTO dto = prestadorService.buscarPerfilPublico(10L);

        assertTrue(dto.isVerificado());
    }

    @Test
    @DisplayName("buscarPerfilPublico: prestador inativo lança ResourceNotFoundException")
    void buscarPerfilPublico_PrestadorInativo_LancaException() {
        usuarioPrestador.setAtivo(false);

        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));

        assertThrows(ResourceNotFoundException.class,
                () -> prestadorService.buscarPerfilPublico(10L));
    }

    @Test
    @DisplayName("buscarPerfilPublico: ID inexistente lança ResourceNotFoundException")
    void buscarPerfilPublico_IdInexistente_LancaException() {
        when(prestadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> prestadorService.buscarPerfilPublico(99L));
    }

    // ── recalcularCompletude ─────────────────────────────────────────────────

    @Test
    @DisplayName("recalcularCompletude: persiste o percentual calculado")
    void recalcularCompletude_AtualizaEPersiste() {
        when(fotoPrestadorRepository.countByPrestadorId(10L)).thenReturn(0L);
        when(prestadorRepository.save(prestador)).thenReturn(prestador);

        prestadorService.recalcularCompletude(prestador);

        verify(prestadorRepository).save(prestador);
    }
}
