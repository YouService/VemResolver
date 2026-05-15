package com.marketplace.servicos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marketplace.servicos.dto.MensagemChatDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.MensagemChat;
import com.marketplace.servicos.model.Prestador;
import com.marketplace.servicos.model.Proposta;
import com.marketplace.servicos.model.SolicitacaoServico;
import com.marketplace.servicos.model.StatusProposta;
import com.marketplace.servicos.model.StatusSolicitacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.MensagemChatRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MensagemChatRepository mensagemRepository;

    @Mock
    private SolicitacaoServicoRepository solicitacaoRepository;

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private ChatService chatService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildUser(Long id, String nome) {
        User u = new User();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(nome.toLowerCase().replace(" ", "") + "@test.com");
        u.setSenha("senha");
        u.setLogin(nome.toLowerCase().replace(" ", ""));
        u.setAtivo(true);
        return u;
    }

    private SolicitacaoServico buildSolicitacao(StatusSolicitacao status, Long clienteId) {
        User cliente = buildUser(clienteId, "Cliente" + clienteId);
        SolicitacaoServico s = new SolicitacaoServico();
        s.setId(10L);
        s.setCliente(cliente);
        s.setStatus(status);
        s.setDescricao("Preciso de ajuda");
        s.setCidade("Sao Paulo");
        s.setEstado("SP");
        return s;
    }

    private Proposta buildProposta(Long prestadorUsuarioId) {
        User prestadorUser = buildUser(prestadorUsuarioId, "Prestador" + prestadorUsuarioId);
        Prestador prestador = new Prestador();
        prestador.setId(prestadorUsuarioId + 100L);
        prestador.setUsuario(prestadorUser);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);
        Proposta p = new Proposta();
        p.setId(20L);
        p.setPrestador(prestador);
        p.setStatus(StatusProposta.ACEITA);
        return p;
    }

    private MensagemChat buildMensagemChat(Long id, Long solicitacaoId, Long remetenteId, String texto) {
        User remetente = buildUser(remetenteId, "User" + remetenteId);
        SolicitacaoServico sol = new SolicitacaoServico();
        sol.setId(solicitacaoId);
        MensagemChat m = new MensagemChat();
        m.setId(id);
        m.setSolicitacao(sol);
        m.setRemetente(remetente);
        m.setTexto(texto);
        m.setDataEnvio(LocalDateTime.now());
        m.setLida(false);
        return m;
    }

    // -------------------------------------------------------------------------
    // CT-01-01
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-01: enviarMensagem com contrato EM_ANDAMENTO persiste mensagem e retorna DTO correto")
    void enviarMensagem_ContratoEmAndamento_PersisteMensagem() {
        // Arrange
        Long solicitacaoId = 10L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String texto = "Olá, quando pode vir?";

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, clienteId);
        solicitacao.setId(solicitacaoId);
        Proposta proposta = buildProposta(prestadorUserId);
        User remetenteUser = buildUser(clienteId, "Cliente1");

        MensagemChat mensagemSalva = buildMensagemChat(10L, solicitacaoId, clienteId, texto);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(propostaRepository.findBySolicitacaoIdAndStatus(solicitacaoId, StatusProposta.ACEITA))
                .thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetenteUser));
        when(mensagemRepository.save(any(MensagemChat.class))).thenReturn(mensagemSalva);

        // Act
        MensagemChatDTO resultado = chatService.enviarMensagem(solicitacaoId, clienteId, texto);

        // Assert
        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(texto, resultado.getTexto());
        verify(mensagemRepository).save(any(MensagemChat.class));
    }

    // -------------------------------------------------------------------------
    // CT-01-02
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-02: marcarComoLidas marca como lidas as mensagens do outro participante")
    void marcarComoLidas_MessagesFromOtherUser_MarkedAsRead() {
        // Arrange — usuarioId=1L is the cliente, so validarParticipante short-circuits (no proposta stub needed)
        Long solicitacaoId = 5L;
        Long usuarioId = 1L;
        Long outroUsuarioId = 2L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, usuarioId);
        solicitacao.setId(solicitacaoId);

        MensagemChat m1 = buildMensagemChat(1L, solicitacaoId, outroUsuarioId, "Mensagem 1");
        MensagemChat m2 = buildMensagemChat(2L, solicitacaoId, outroUsuarioId, "Mensagem 2");

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(mensagemRepository.findBySolicitacaoIdAndRemetenteIdNotAndLidaFalse(solicitacaoId, usuarioId))
                .thenReturn(List.of(m1, m2));

        // Act
        chatService.marcarComoLidas(solicitacaoId, usuarioId);

        // Assert
        assertTrue(m1.getLida());
        assertTrue(m2.getLida());
        ArgumentCaptor<List<MensagemChat>> captor = ArgumentCaptor.forClass(List.class);
        verify(mensagemRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    // -------------------------------------------------------------------------
    // CT-01-03
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-03: enviarMensagem com contrato CONCLUIDA lanca BusinessException")
    void enviarMensagem_ContratoConcluidaThrowsBusinessException() {
        // Arrange — remetente=clienteId so validarParticipante short-circuits; no proposta stub needed
        Long solicitacaoId = 10L;
        Long clienteId = 1L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.CONCLUIDA, clienteId);
        solicitacao.setId(solicitacaoId);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(solicitacaoId, clienteId, "Oi"));
        assertTrue(ex.getMessage().contains("O chat só está disponível"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-04
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-04: enviarMensagem com contrato CANCELADA lanca BusinessException")
    void enviarMensagem_ContratoCanceladaThrowsBusinessException() {
        // Arrange — remetente=clienteId so validarParticipante short-circuits; no proposta stub needed
        Long solicitacaoId = 10L;
        Long clienteId = 1L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.CANCELADA, clienteId);
        solicitacao.setId(solicitacaoId);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(solicitacaoId, clienteId, "Oi"));
        assertTrue(ex.getMessage().contains("O chat só está disponível"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-05
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-05: enviarMensagem com texto em branco lanca BusinessException")
    void enviarMensagem_TextoVazioThrowsBusinessException() {
        // Arrange — remetente=clienteId so validarParticipante short-circuits; no proposta stub needed
        Long solicitacaoId = 10L;
        Long clienteId = 1L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, clienteId);
        solicitacao.setId(solicitacaoId);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(solicitacaoId, clienteId, "   "));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-06
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-06: enviarMensagem com texto de 2001 caracteres lanca BusinessException")
    void enviarMensagem_Texto2001CaracteresThrowsBusinessException() {
        // Arrange — remetente=clienteId so validarParticipante short-circuits; no proposta stub needed
        Long solicitacaoId = 10L;
        Long clienteId = 1L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, clienteId);
        solicitacao.setId(solicitacaoId);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));

        String textoLongo = "A".repeat(2001);

        // Act & Assert
        assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(solicitacaoId, clienteId, textoLongo));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-07
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-07: enviarMensagem com texto de exatamente 2000 caracteres e aceito")
    void enviarMensagem_Texto2000CaracteresAceito() {
        // Arrange
        Long solicitacaoId = 10L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String texto = "A".repeat(2000);

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, clienteId);
        solicitacao.setId(solicitacaoId);
        Proposta proposta = buildProposta(prestadorUserId);
        User remetenteUser = buildUser(clienteId, "Cliente1");
        MensagemChat mensagemSalva = buildMensagemChat(11L, solicitacaoId, clienteId, texto);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(propostaRepository.findBySolicitacaoIdAndStatus(solicitacaoId, StatusProposta.ACEITA))
                .thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetenteUser));
        when(mensagemRepository.save(any(MensagemChat.class))).thenReturn(mensagemSalva);

        // Act
        MensagemChatDTO resultado = chatService.enviarMensagem(solicitacaoId, clienteId, texto);

        // Assert
        assertNotNull(resultado);
        assertEquals(11L, resultado.getId());
    }

    // -------------------------------------------------------------------------
    // CT-01-08
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-08: enviarMensagem por usuario nao participante lanca BusinessException com Acesso negado")
    void enviarMensagem_UsuarioNaoParticipanteThrowsBusinessException() {
        // Arrange
        Long solicitacaoId = 10L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        Long intruso = 99L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, clienteId);
        solicitacao.setId(solicitacaoId);
        Proposta proposta = buildProposta(prestadorUserId);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(propostaRepository.findBySolicitacaoIdAndStatus(solicitacaoId, StatusProposta.ACEITA))
                .thenReturn(Optional.of(proposta));

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(solicitacaoId, intruso, "Tentando acessar"));
        assertTrue(ex.getMessage().contains("Acesso negado"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-09
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-09: listarMensagens com contrato CONCLUIDO retorna historico sem lancsar excecao")
    void listarMensagens_ContratoConcluidoRetornaHistorico() {
        // Arrange — caller=clienteId so validarParticipante short-circuits; no proposta stub needed.
        // listarMensagens has no status restriction, so CONCLUIDA must not throw.
        Long solicitacaoId = 10L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.CONCLUIDA, clienteId);
        solicitacao.setId(solicitacaoId);

        MensagemChat m1 = buildMensagemChat(1L, solicitacaoId, clienteId, "Oi");
        MensagemChat m2 = buildMensagemChat(2L, solicitacaoId, prestadorUserId, "Ola");
        MensagemChat m3 = buildMensagemChat(3L, solicitacaoId, clienteId, "Obrigado");

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(mensagemRepository.findBySolicitacaoIdOrderByDataEnvioAsc(solicitacaoId))
                .thenReturn(List.of(m1, m2, m3));

        // Act
        List<MensagemChatDTO> resultado = chatService.listarMensagens(solicitacaoId, clienteId);

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
    }

    // -------------------------------------------------------------------------
    // CT-01-10
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-10: enviarMensagem cria notificacao para o destinatario correto")
    void enviarMensagem_NotificacaoCriadaParaDestinatario() {
        // Arrange
        Long solicitacaoId = 10L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String texto = "Oi";

        SolicitacaoServico solicitacao = buildSolicitacao(StatusSolicitacao.EM_ANDAMENTO, clienteId);
        solicitacao.setId(solicitacaoId);
        Proposta proposta = buildProposta(prestadorUserId);
        User remetenteUser = buildUser(clienteId, "Cliente1");
        MensagemChat mensagemSalva = buildMensagemChat(12L, solicitacaoId, clienteId, texto);

        when(solicitacaoRepository.findById(solicitacaoId)).thenReturn(Optional.of(solicitacao));
        when(propostaRepository.findBySolicitacaoIdAndStatus(solicitacaoId, StatusProposta.ACEITA))
                .thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetenteUser));
        when(mensagemRepository.save(any(MensagemChat.class))).thenReturn(mensagemSalva);

        // Act
        chatService.enviarMensagem(solicitacaoId, clienteId, texto);

        // Assert
        verify(notificacaoService).criar(
                eq(prestadorUserId),
                contains("Nova mensagem"),
                eq("NOVA_MENSAGEM_CHAT"),
                anyString()
        );
    }
}
