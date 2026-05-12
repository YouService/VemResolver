package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PrestadorDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Categoria;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.CategoriaRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private PrestadorService prestadorService;

    private User usuarioPrestador;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        usuarioPrestador = new User();
        usuarioPrestador.setId(1L);
        usuarioPrestador.setNome("Maria");
        usuarioPrestador.setEmail("maria@prestador.com");
        usuarioPrestador.setTipo(TipoUsuario.PRESTADOR);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Eletricista");
    }

    @Test
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
        when(prestadorRepository.save(any(Prestador.class))).thenAnswer(inv -> {
            Prestador p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PrestadorDTO resultado = prestadorService.criarOuAtualizar(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getUsuarioId());
        verify(prestadorRepository).save(any(Prestador.class));
    }

    @Test
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

    @Test
    void buscarPorId_PrestadorExistente_RetornaDTO() {
        Prestador prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(usuarioPrestador);
        prestador.setCategorias(List.of(categoria));
        prestador.setCidade("São Paulo");
        prestador.setEstado("SP");

        when(prestadorRepository.findById(10L)).thenReturn(Optional.of(prestador));

        PrestadorDTO resultado = prestadorService.buscarPorId(10L);

        assertEquals(10L, resultado.getId());
        assertEquals(1L, resultado.getUsuarioId());
    }

    @Test
    void buscarPorId_PrestadorInexistente_LancaException() {
        when(prestadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> prestadorService.buscarPorId(99L));
    }
}
