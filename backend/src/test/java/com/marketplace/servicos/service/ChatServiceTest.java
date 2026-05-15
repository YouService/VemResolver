package com.marketplace.servicos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.marketplace.servicos.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private MensagemChatRepository mensagemRepository;
    @Mock private PropostaRepository propostaRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificacaoService notificacaoService;

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

    private Proposta buildProposta(Long propostaId, StatusProposta status,
                                    Long clienteId, Long prestadorUserId,
                                    StatusSolicitacao solicitacaoStatus) {
        User clienteUser = buildUser(clienteId, "Cliente" + clienteId);
        User prestadorUser = buildUser(prestadorUserId, "Prestador" + prestadorUserId);

        Prestador prestador = new Prestador();
        prestador.setId(prestadorUserId + 100L);
        prestador.setUsuario(prestadorUser);
        prestador.setAvaliacaoMedia(0.0);
        prestador.setTotalAvaliacoes(0);

        SolicitacaoServico solicitacao = new SolicitacaoServico();
        solicitacao.setId(10L);
        solicitacao.setCliente(clienteUser);
        solicitacao.setStatus(solicitacaoStatus);
        solicitacao.setDescricao("Preciso de ajuda");
        solicitacao.setCidade("Sao Paulo");
        solicitacao.setEstado("SP");

        Proposta p = new Proposta();
        p.setId(propostaId);
        p.setPrestador(prestador);
        p.setSolicitacao(solicitacao);
        p.setStatus(status);
        return p;
    }

    private MensagemChat buildMensagemChat(Long id, Proposta proposta, Long remetenteId, String texto) {
        User remetente = buildUser(remetenteId, "User" + remetenteId);
        MensagemChat m = new MensagemChat();
        m.setId(id);
        m.setProposta(proposta);
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
    @DisplayName("CT-01-01: enviarMensagem com proposta ENVIADA persiste mensagem e retorna DTO correto")
    void enviarMensagem_PropostaEnviada_PersisteMensagem() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String texto = "Olá, quando pode vir?";

        Proposta proposta = buildProposta(propostaId, StatusProposta.ENVIADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);
        User remetenteUser = buildUser(clienteId, "Cliente1");
        MensagemChat mensagemSalva = buildMensagemChat(10L, proposta, clienteId, texto);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetenteUser));
        when(mensagemRepository.save(any(MensagemChat.class))).thenReturn(mensagemSalva);

        MensagemChatDTO resultado = chatService.enviarMensagem(propostaId, clienteId, texto);

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
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.EM_ANDAMENTO);
        MensagemChat m1 = buildMensagemChat(1L, proposta, prestadorUserId, "Mensagem 1");
        MensagemChat m2 = buildMensagemChat(2L, proposta, prestadorUserId, "Mensagem 2");

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(mensagemRepository.findByPropostaIdAndRemetenteIdNotAndLidaFalse(propostaId, clienteId))
                .thenReturn(List.of(m1, m2));

        chatService.marcarComoLidas(propostaId, clienteId);

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
    @DisplayName("CT-01-03: enviarMensagem com solicitacao CONCLUIDA lanca BusinessException")
    void enviarMensagem_SolicitacaoConcluida_ThrowsBusinessException() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.CONCLUIDA);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(propostaId, clienteId, "Oi"));
        assertTrue(ex.getMessage().contains("contratos ativos"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-04
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-04: enviarMensagem com solicitacao CANCELADA lanca BusinessException")
    void enviarMensagem_SolicitacaoCancelada_ThrowsBusinessException() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.CANCELADA);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(propostaId, clienteId, "Oi"));
        assertTrue(ex.getMessage().contains("contratos ativos"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-05
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-05: enviarMensagem com proposta RECUSADA lanca BusinessException")
    void enviarMensagem_PropostaRecusada_ThrowsBusinessException() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.RECUSADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(propostaId, clienteId, "Oi"));
        assertTrue(ex.getMessage().contains("recusadas"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-06
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-06: enviarMensagem com texto em branco lanca BusinessException")
    void enviarMensagem_TextoVazio_ThrowsBusinessException() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.ENVIADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(propostaId, clienteId, "   "));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-07
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-07: enviarMensagem com texto de 2001 caracteres lanca BusinessException")
    void enviarMensagem_Texto2001Caracteres_ThrowsBusinessException() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String textoLongo = "A".repeat(2001);

        Proposta proposta = buildProposta(propostaId, StatusProposta.ENVIADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(propostaId, clienteId, textoLongo));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-08
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-08: enviarMensagem com texto de exatamente 2000 caracteres e aceito")
    void enviarMensagem_Texto2000Caracteres_Aceito() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String texto = "A".repeat(2000);

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.EM_ANDAMENTO);
        User remetenteUser = buildUser(clienteId, "Cliente1");
        MensagemChat mensagemSalva = buildMensagemChat(11L, proposta, clienteId, texto);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetenteUser));
        when(mensagemRepository.save(any(MensagemChat.class))).thenReturn(mensagemSalva);

        MensagemChatDTO resultado = chatService.enviarMensagem(propostaId, clienteId, texto);

        assertNotNull(resultado);
        assertEquals(11L, resultado.getId());
    }

    // -------------------------------------------------------------------------
    // CT-01-09
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-09: enviarMensagem por usuario nao participante lanca BusinessException com Acesso negado")
    void enviarMensagem_UsuarioNaoParticipante_ThrowsBusinessException() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        Long intruso = 99L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.ENVIADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.enviarMensagem(propostaId, intruso, "Tentando acessar"));
        assertTrue(ex.getMessage().contains("Acesso negado"));
        verify(mensagemRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // CT-01-10
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-10: listarMensagens retorna historico sem lancar excecao mesmo com solicitacao CONCLUIDA")
    void listarMensagens_SolicitacaoConcluida_RetornaHistorico() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.CONCLUIDA);
        MensagemChat m1 = buildMensagemChat(1L, proposta, clienteId, "Oi");
        MensagemChat m2 = buildMensagemChat(2L, proposta, prestadorUserId, "Ola");
        MensagemChat m3 = buildMensagemChat(3L, proposta, clienteId, "Obrigado");

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(mensagemRepository.findByPropostaIdOrderByDataEnvioAsc(propostaId))
                .thenReturn(List.of(m1, m2, m3));

        List<MensagemChatDTO> resultado = chatService.listarMensagens(propostaId, clienteId);

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
    }

    // -------------------------------------------------------------------------
    // CT-01-11
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("CT-01-11: enviarMensagem cria notificacao para o destinatario correto")
    void enviarMensagem_NotificacaoCriadaParaDestinatario() {
        Long propostaId = 20L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        String texto = "Oi";

        Proposta proposta = buildProposta(propostaId, StatusProposta.ENVIADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);
        User remetenteUser = buildUser(clienteId, "Cliente1");
        MensagemChat mensagemSalva = buildMensagemChat(12L, proposta, clienteId, texto);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetenteUser));
        when(mensagemRepository.save(any(MensagemChat.class))).thenReturn(mensagemSalva);

        chatService.enviarMensagem(propostaId, clienteId, texto);

        verify(notificacaoService).criar(
                eq(prestadorUserId),
                contains("Nova mensagem"),
                eq("NOVA_MENSAGEM_CHAT"),
                anyString()
        );
    }
}
