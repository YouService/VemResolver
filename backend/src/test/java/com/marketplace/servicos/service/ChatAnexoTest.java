package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.MensagemChatDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.MensagemChatRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatAnexoTest {

    @Mock private MensagemChatRepository mensagemRepository;
    @Mock private PropostaRepository propostaRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks
    private ChatService chatService;

    @TempDir
    Path tempDir;

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

    private Proposta buildProposta(Long propostaId, StatusProposta statusProposta,
                                   Long clienteId, Long prestadorUserId,
                                   StatusSolicitacao statusSolicitacao) {
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
        solicitacao.setStatus(statusSolicitacao);
        solicitacao.setDescricao("Preciso de ajuda");
        solicitacao.setCidade("Sao Paulo");
        solicitacao.setEstado("SP");

        Proposta p = new Proposta();
        p.setId(propostaId);
        p.setPrestador(prestador);
        p.setSolicitacao(solicitacao);
        p.setStatus(statusProposta);
        return p;
    }

    private MensagemChat buildMensagemChat(Long id, Proposta proposta, Long remetenteId, String url) {
        User remetente = buildUser(remetenteId, "User" + remetenteId);
        MensagemChat m = new MensagemChat();
        m.setId(id);
        m.setProposta(proposta);
        m.setRemetente(remetente);
        m.setTexto("");
        m.setUrlAnexo(url);
        m.setDataEnvio(LocalDateTime.now());
        m.setLida(false);
        return m;
    }

    // CA-01: uploadImagem com arquivo válido salva mensagem com urlAnexo e retorna DTO
    @Test
    @DisplayName("CA-01: uploadImagem com arquivo válido → salva mensagem com urlAnexo e retorna DTO")
    void uploadImagem_ArquivoValido_RetornaDTOComUrlAnexo() {
        Long propostaId = 1L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        ReflectionTestUtils.setField(chatService, "uploadDir", tempDir.toString());

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.EM_ANDAMENTO);
        User remetente = buildUser(clienteId, "Cliente1");

        MockMultipartFile file = new MockMultipartFile(
                "file", "imagem.jpg", "image/jpeg", new byte[1024]);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));
        when(userRepository.findById(clienteId)).thenReturn(Optional.of(remetente));
        when(mensagemRepository.save(any(MensagemChat.class))).thenAnswer(inv -> {
            MensagemChat msg = inv.getArgument(0);
            msg.setId(100L);
            msg.setDataEnvio(LocalDateTime.now());
            return msg;
        });

        MensagemChatDTO resultado = chatService.uploadImagem(propostaId, clienteId, file);

        assertNotNull(resultado);
        assertNotNull(resultado.getUrlAnexo());
        assertTrue(resultado.getUrlAnexo().contains("/uploads/chat/"));
        verify(mensagemRepository).save(any(MensagemChat.class));
        verify(notificacaoService).criar(eq(prestadorUserId), any(), eq("NOVA_MENSAGEM_CHAT"), any());
    }

    // CA-02: uploadImagem com arquivo > 5MB lança BusinessException
    @Test
    @DisplayName("CA-02: uploadImagem com arquivo > 5MB → lança BusinessException")
    void uploadImagem_ArquivoMaiorQue5MB_LancaBusinessException() {
        Long propostaId = 1L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        ReflectionTestUtils.setField(chatService, "uploadDir", tempDir.toString());

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.EM_ANDAMENTO);

        byte[] bigContent = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile file = new MockMultipartFile("file", "grande.jpg", "image/jpeg", bigContent);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.uploadImagem(propostaId, clienteId, file));
        assertTrue(ex.getMessage().contains("5 MB"));
        verify(mensagemRepository, never()).save(any());
    }

    // CA-03: uploadImagem com tipo inválido (PDF) lança BusinessException
    @Test
    @DisplayName("CA-03: uploadImagem com tipo inválido (PDF) → lança BusinessException")
    void uploadImagem_TipoInvalido_LancaBusinessException() {
        Long propostaId = 1L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        ReflectionTestUtils.setField(chatService, "uploadDir", tempDir.toString());

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.EM_ANDAMENTO);

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[1024]);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.uploadImagem(propostaId, clienteId, file));
        assertTrue(ex.getMessage().contains("Formato inválido"));
        verify(mensagemRepository, never()).save(any());
    }

    // CA-04: uploadImagem em proposta RECUSADA lança BusinessException
    @Test
    @DisplayName("CA-04: uploadImagem em proposta RECUSADA → lança BusinessException")
    void uploadImagem_PropostaRecusada_LancaBusinessException() {
        Long propostaId = 1L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;

        ReflectionTestUtils.setField(chatService, "uploadDir", tempDir.toString());

        Proposta proposta = buildProposta(propostaId, StatusProposta.RECUSADA, clienteId, prestadorUserId, StatusSolicitacao.ABERTA);

        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", new byte[1024]);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.uploadImagem(propostaId, clienteId, file));
        assertTrue(ex.getMessage().contains("recusadas"));
        verify(mensagemRepository, never()).save(any());
    }

    // CA-05: uploadImagem sem ser participante lança BusinessException
    @Test
    @DisplayName("CA-05: uploadImagem sem ser participante → lança BusinessException")
    void uploadImagem_UsuarioNaoParticipante_LancaBusinessException() {
        Long propostaId = 1L;
        Long clienteId = 1L;
        Long prestadorUserId = 2L;
        Long intruso = 99L;

        ReflectionTestUtils.setField(chatService, "uploadDir", tempDir.toString());

        Proposta proposta = buildProposta(propostaId, StatusProposta.ACEITA, clienteId, prestadorUserId, StatusSolicitacao.EM_ANDAMENTO);

        MockMultipartFile file = new MockMultipartFile("file", "img.jpg", "image/jpeg", new byte[1024]);

        when(propostaRepository.findById(propostaId)).thenReturn(Optional.of(proposta));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> chatService.uploadImagem(propostaId, intruso, file));
        assertTrue(ex.getMessage().contains("Acesso negado"));
        verify(mensagemRepository, never()).save(any());
    }
}
