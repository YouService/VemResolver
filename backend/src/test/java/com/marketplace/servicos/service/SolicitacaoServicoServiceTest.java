package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private SolicitacaoServicoService solicitacaoService;

    private User cliente;
    private Categoria categoria;
    private SolicitacaoServico solicitacao;

    @BeforeEach
    void setUp() {
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
}
