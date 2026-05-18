package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.NotificacaoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Notificacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.NotificacaoRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock private NotificacaoRepository notificacaoRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);
        usuario.setNome("João");
        usuario.setEmail("joao@teste.com");
    }

    // ── criar ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("criar: usuário existente — persiste notificação com dados corretos")
    void criar_UsuarioExistente_SalvaNotificacao() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(inv -> {
            Notificacao n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });

        notificacaoService.criar(1L, "Nova proposta recebida", "NOVA_PROPOSTA", "/cliente/dashboard");

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        Notificacao salva = captor.getValue();
        assertEquals("Nova proposta recebida", salva.getMensagem());
        assertEquals("NOVA_PROPOSTA", salva.getTipo());
        assertEquals("/cliente/dashboard", salva.getLinkDestino());
        assertEquals(usuario, salva.getUsuario());
    }

    @Test
    @DisplayName("criar: usuário inexistente lança ResourceNotFoundException")
    void criar_UsuarioInexistente_LancaException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificacaoService.criar(99L, "msg", "TIPO", "/link"));
        verify(notificacaoRepository, never()).save(any());
    }

    // ── marcarComoLida ───────────────────────────────────────────────────────

    @Test
    @DisplayName("marcarComoLida: dono correto marca como lida")
    void marcarComoLida_DonoCorreto_MarcaComoLida() {
        Notificacao n = buildNotificacao(10L, usuario, false);
        when(notificacaoRepository.findById(10L)).thenReturn(Optional.of(n));

        notificacaoService.marcarComoLida(10L, 1L);

        assertTrue(n.getLida());
        verify(notificacaoRepository).save(n);
    }

    @Test
    @DisplayName("marcarComoLida: outro usuário tentando marcar lança BusinessException")
    void marcarComoLida_OutroUsuario_LancaException() {
        Notificacao n = buildNotificacao(10L, usuario, false);
        when(notificacaoRepository.findById(10L)).thenReturn(Optional.of(n));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> notificacaoService.marcarComoLida(10L, 999L));
        assertEquals("Acesso negado", ex.getMessage());
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("marcarComoLida: notificação inexistente lança ResourceNotFoundException")
    void marcarComoLida_NotificacaoInexistente_LancaException() {
        when(notificacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> notificacaoService.marcarComoLida(99L, 1L));
    }

    // ── marcarTodasComoLidas ─────────────────────────────────────────────────

    @Test
    @DisplayName("marcarTodasComoLidas: marca todas as não lidas do usuário")
    void marcarTodasComoLidas_MarcaTodasDoUsuario() {
        Notificacao n1 = buildNotificacao(1L, usuario, false);
        Notificacao n2 = buildNotificacao(2L, usuario, false);

        when(notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(n1, n2));

        notificacaoService.marcarTodasComoLidas(1L);

        assertTrue(n1.getLida());
        assertTrue(n2.getLida());
        verify(notificacaoRepository, times(2)).save(any(Notificacao.class));
    }

    @Test
    @DisplayName("marcarTodasComoLidas: sem notificações não lidas não chama save")
    void marcarTodasComoLidas_SemNotificacoes_NaoCallSave() {
        when(notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of());

        notificacaoService.marcarTodasComoLidas(1L);

        verify(notificacaoRepository, never()).save(any());
    }

    // ── listarNaoLidas ────────────────────────────────────────────────────────

    @Test
    @DisplayName("listarNaoLidas: retorna DTOs das notificações não lidas")
    void listarNaoLidas_RetornaDTOs() {
        Notificacao n1 = buildNotificacao(1L, usuario, false);
        Notificacao n2 = buildNotificacao(2L, usuario, false);

        when(notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(n1, n2));

        List<NotificacaoDTO> resultado = notificacaoService.listarNaoLidas(1L);

        assertEquals(2, resultado.size());
        resultado.forEach(dto -> assertFalse(dto.getLida()));
    }

    // ── contarNaoLidas ────────────────────────────────────────────────────────

    @Test
    @DisplayName("contarNaoLidas: retorna a contagem do repositório")
    void contarNaoLidas_RetornaContagem() {
        when(notificacaoRepository.countByUsuarioIdAndLidaFalse(1L)).thenReturn(3L);

        long count = notificacaoService.contarNaoLidas(1L);

        assertEquals(3L, count);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private Notificacao buildNotificacao(Long id, User user, boolean lida) {
        Notificacao n = new Notificacao();
        n.setId(id);
        n.setUsuario(user);
        n.setMensagem("Mensagem " + id);
        n.setTipo("TIPO");
        n.setLida(lida);
        n.setLinkDestino("/link");
        return n;
    }
}
