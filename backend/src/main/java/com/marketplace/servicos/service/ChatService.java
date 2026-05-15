package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.MensagemChatDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.MensagemChat;
import com.marketplace.servicos.model.Proposta;
import com.marketplace.servicos.model.StatusProposta;
import com.marketplace.servicos.model.StatusSolicitacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.MensagemChatRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final MensagemChatRepository mensagemRepository;
    private final PropostaRepository propostaRepository;
    private final UserRepository userRepository;
    private final NotificacaoService notificacaoService;

    public ChatService(MensagemChatRepository mensagemRepository,
                       PropostaRepository propostaRepository,
                       UserRepository userRepository,
                       NotificacaoService notificacaoService) {
        this.mensagemRepository = mensagemRepository;
        this.propostaRepository = propostaRepository;
        this.userRepository = userRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public List<MensagemChatDTO> listarMensagens(Long propostaId, Long usuarioId) {
        Proposta proposta = buscarPropostaValidada(propostaId);
        validarParticipante(proposta, usuarioId);
        return mensagemRepository.findByPropostaIdOrderByDataEnvioAsc(propostaId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public MensagemChatDTO enviarMensagem(Long propostaId, Long remetenteId, String texto) {
        Proposta proposta = buscarPropostaValidada(propostaId);
        validarParticipante(proposta, remetenteId);

        if (proposta.getStatus() == StatusProposta.RECUSADA) {
            throw new BusinessException("Não é possível enviar mensagens em propostas recusadas");
        }

        StatusSolicitacao statusSolicitacao = proposta.getSolicitacao().getStatus();
        if (statusSolicitacao == StatusSolicitacao.CONCLUIDA || statusSolicitacao == StatusSolicitacao.CANCELADA) {
            throw new BusinessException("O chat está disponível apenas para contratos ativos");
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
        mensagem.setProposta(proposta);
        mensagem.setRemetente(remetente);
        mensagem.setTexto(texto);
        mensagem.setLida(false);
        MensagemChat salva = mensagemRepository.save(mensagem);

        Long clienteId = proposta.getSolicitacao().getCliente().getId();
        Long prestadorUsuarioId = proposta.getPrestador().getUsuario().getId();
        Long destinatarioId = remetenteId.equals(clienteId) ? prestadorUsuarioId : clienteId;

        notificacaoService.criar(
                destinatarioId,
                "Nova mensagem de " + remetente.getNome(),
                "NOVA_MENSAGEM_CHAT",
                "/chat/proposta/" + propostaId
        );

        log.info("Mensagem enviada: propostaId={} remetenteId={}", propostaId, remetenteId);
        return toDTO(salva);
    }

    public void marcarComoLidas(Long propostaId, Long usuarioId) {
        Proposta proposta = buscarPropostaValidada(propostaId);
        validarParticipante(proposta, usuarioId);

        List<MensagemChat> naoLidas = mensagemRepository
                .findByPropostaIdAndRemetenteIdNotAndLidaFalse(propostaId, usuarioId);
        naoLidas.forEach(m -> m.setLida(true));
        mensagemRepository.saveAll(naoLidas);
        log.info("Mensagens marcadas como lidas: propostaId={} usuarioId={} quantidade={}", propostaId, usuarioId, naoLidas.size());
    }

    private Proposta buscarPropostaValidada(Long propostaId) {
        return propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));
    }

    private void validarParticipante(Proposta proposta, Long usuarioId) {
        Long clienteId = proposta.getSolicitacao().getCliente().getId();
        Long prestadorUsuarioId = proposta.getPrestador().getUsuario().getId();
        if (!usuarioId.equals(clienteId) && !usuarioId.equals(prestadorUsuarioId)) {
            throw new BusinessException("Acesso negado");
        }
    }

    private MensagemChatDTO toDTO(MensagemChat m) {
        MensagemChatDTO dto = new MensagemChatDTO();
        dto.setId(m.getId());
        dto.setPropostaId(m.getProposta().getId());
        dto.setRemetenteId(m.getRemetente().getId());
        dto.setRemetenteNome(m.getRemetente().getNome());
        dto.setTexto(m.getTexto());
        dto.setDataEnvio(m.getDataEnvio());
        dto.setLida(m.getLida());
        return dto;
    }
}
