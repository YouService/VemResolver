package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.NotificacaoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Notificacao;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.NotificacaoRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);

    private final NotificacaoRepository notificacaoRepository;
    private final UserRepository userRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UserRepository userRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.userRepository = userRepository;
    }

    public void criar(Long usuarioId, String mensagem, String tipo, String linkDestino) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Notificacao n = new Notificacao();
        n.setUsuario(usuario);
        n.setMensagem(mensagem);
        n.setTipo(tipo);
        n.setLinkDestino(linkDestino);
        notificacaoRepository.save(n);
        log.info("Notificacao criada para usuario={} tipo={}", usuarioId, tipo);
    }

    @Transactional(readOnly = true)
    public List<NotificacaoDTO> listarNaoLidas(Long usuarioId) {
        return notificacaoRepository
                .findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(usuarioId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoDTO> listarTodas(Long usuarioId, Pageable pageable) {
        return notificacaoRepository
                .findByUsuarioIdOrderByDataCriacaoDesc(usuarioId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    public void marcarComoLida(Long id, Long usuarioId) {
        Notificacao n = notificacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        if (!n.getUsuario().getId().equals(usuarioId)) {
            throw new BusinessException("Acesso negado");
        }
        n.setLida(true);
        notificacaoRepository.save(n);
        log.info("Notificacao id={} marcada como lida pelo usuario={}", id, usuarioId);
    }

    public void marcarTodasComoLidas(Long usuarioId) {
        notificacaoRepository
                .findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(usuarioId)
                .forEach(n -> {
                    n.setLida(true);
                    notificacaoRepository.save(n);
                });
        log.info("Todas as notificacoes marcadas como lidas para usuario={}", usuarioId);
    }

    private NotificacaoDTO toDTO(Notificacao n) {
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setId(n.getId());
        dto.setMensagem(n.getMensagem());
        dto.setTipo(n.getTipo());
        dto.setLida(n.getLida());
        dto.setLinkDestino(n.getLinkDestino());
        dto.setDataCriacao(n.getDataCriacao());
        return dto;
    }
}
