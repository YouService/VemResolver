package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.MensagemChatDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.MensagemChat;
import com.marketplace.servicos.model.Proposta;
import com.marketplace.servicos.model.SolicitacaoServico;
import com.marketplace.servicos.model.StatusProposta;
import com.marketplace.servicos.model.StatusSolicitacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.MensagemChatRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final MensagemChatRepository mensagemRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final PropostaRepository propostaRepository;
    private final UserRepository userRepository;
    private final NotificacaoService notificacaoService;

    public ChatService(MensagemChatRepository mensagemRepository,
                       SolicitacaoServicoRepository solicitacaoRepository,
                       PropostaRepository propostaRepository,
                       UserRepository userRepository,
                       NotificacaoService notificacaoService) {
        this.mensagemRepository = mensagemRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.propostaRepository = propostaRepository;
        this.userRepository = userRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<MensagemChatDTO> listarMensagens(Long solicitacaoId, Long usuarioId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        validarParticipante(solicitacao, usuarioId);
        return mensagemRepository.findBySolicitacaoIdOrderByDataEnvioAsc(solicitacaoId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public MensagemChatDTO enviarMensagem(Long solicitacaoId, Long remetenteId, String texto) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        validarParticipante(solicitacao, remetenteId);

        if (solicitacao.getStatus() != StatusSolicitacao.EM_ANDAMENTO) {
            throw new BusinessException("O chat só está disponível para contratos em andamento");
        }

        if (texto == null || texto.isBlank()) {
            throw new BusinessException("Texto da mensagem é obrigatório");
        }
        if (texto.length() > 2000) {
            throw new BusinessException("Texto deve ter no máximo 2000 caracteres");
        }

        User remetente = userRepository.findById(remetenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        MensagemChat mensagem = new MensagemChat();
        mensagem.setSolicitacao(solicitacao);
        mensagem.setRemetente(remetente);
        mensagem.setTexto(texto);
        mensagem.setLida(false);
        MensagemChat salva = mensagemRepository.save(mensagem);

        Long clienteId = solicitacao.getCliente().getId();
        Optional<Proposta> propostaAceita = propostaRepository.findBySolicitacaoIdAndStatus(
                solicitacaoId, StatusProposta.ACEITA);
        Long destinatarioId;
        if (remetenteId.equals(clienteId)) {
            destinatarioId = propostaAceita
                    .map(p -> p.getPrestador().getUsuario().getId())
                    .orElse(null);
        } else {
            destinatarioId = clienteId;
        }

        if (destinatarioId != null) {
            notificacaoService.criar(
                    destinatarioId,
                    "Nova mensagem de " + remetente.getNome(),
                    "NOVA_MENSAGEM_CHAT",
                    "/chat/solicitacao/" + solicitacaoId
            );
        }

        log.info("Mensagem enviada: solicitacaoId={} remetenteId={}", solicitacaoId, remetenteId);
        return toDTO(salva);
    }

    public void marcarComoLidas(Long solicitacaoId, Long usuarioId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        validarParticipante(solicitacao, usuarioId);

        List<MensagemChat> naoLidas = mensagemRepository
                .findBySolicitacaoIdAndRemetenteIdNotAndLidaFalse(solicitacaoId, usuarioId);
        naoLidas.forEach(m -> m.setLida(true));
        mensagemRepository.saveAll(naoLidas);
        log.info("Mensagens marcadas como lidas: solicitacaoId={} usuarioId={} quantidade={}", solicitacaoId, usuarioId, naoLidas.size());
    }

    private void validarParticipante(SolicitacaoServico solicitacao, Long usuarioId) {
        Long clienteId = solicitacao.getCliente().getId();
        if (usuarioId.equals(clienteId)) {
            return;
        }
        Optional<Proposta> propostaAceita = propostaRepository.findBySolicitacaoIdAndStatus(
                solicitacao.getId(), StatusProposta.ACEITA);
        if (propostaAceita.isEmpty()) {
            throw new BusinessException("Acesso negado");
        }
        Long prestadorUsuarioId = propostaAceita.get().getPrestador().getUsuario().getId();
        if (!usuarioId.equals(prestadorUsuarioId)) {
            throw new BusinessException("Acesso negado");
        }
    }

    private MensagemChatDTO toDTO(MensagemChat m) {
        MensagemChatDTO dto = new MensagemChatDTO();
        dto.setId(m.getId());
        dto.setSolicitacaoId(m.getSolicitacao().getId());
        dto.setRemetenteId(m.getRemetente().getId());
        dto.setRemetenteNome(m.getRemetente().getNome());
        dto.setTexto(m.getTexto());
        dto.setDataEnvio(m.getDataEnvio());
        dto.setLida(m.getLida());
        return dto;
    }
}
