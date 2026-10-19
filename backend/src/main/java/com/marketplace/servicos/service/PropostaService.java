package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PropostaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropostaService {

    private static final Logger log = LoggerFactory.getLogger(PropostaService.class);

    private final PropostaRepository propostaRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final PrestadorRepository prestadorRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotificacaoService notificacaoService;
    private final EmailService emailService;
    private final CreditoService creditoService;

    @Autowired @Lazy
    private WebPushService webPushService;

    public PropostaService(PropostaRepository propostaRepository,
                           SolicitacaoServicoRepository solicitacaoRepository,
                           PrestadorRepository prestadorRepository,
                           AvaliacaoRepository avaliacaoRepository,
                           NotificacaoService notificacaoService,
                           EmailService emailService,
                           CreditoService creditoService) {
        this.propostaRepository = propostaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.prestadorRepository = prestadorRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notificacaoService = notificacaoService;
        this.emailService = emailService;
        this.creditoService = creditoService;
    }
    
    @Transactional
    public PropostaDTO criar(PropostaDTO dto, Long usuarioId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new BusinessException("Esta solicitação não está mais aberta para propostas");
        }

        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador não encontrado"));
        
        if (propostaRepository.existsBySolicitacaoIdAndPrestadorId(solicitacao.getId(), prestador.getId())) {
            throw new BusinessException("Você já enviou uma proposta para esta solicitação");
        }

        creditoService.validarCreditoParaProposta(prestador);
        
        Proposta proposta = new Proposta();
        proposta.setSolicitacao(solicitacao);
        proposta.setPrestador(prestador);
        proposta.setValor(dto.getValor());
        proposta.setMensagem(dto.getMensagem());
        
        proposta = propostaRepository.save(proposta);

        notificacaoService.criar(
            solicitacao.getCliente().getId(),
            "Nova proposta de " + prestador.getUsuario().getNome() + " para sua solicitação: " + solicitacao.getDescricao().substring(0, Math.min(50, solicitacao.getDescricao().length())),
            "NOVA_PROPOSTA",
            "/cliente/solicitacao/" + solicitacao.getId() + "/propostas"
        );

        emailService.sendPropostaRecebida(
            solicitacao.getCliente().getEmail(),
            prestador.getUsuario().getNome(),
            solicitacao.getDescricao()
        );

        webPushService.notificar(
            solicitacao.getCliente().getId(),
            "Nova proposta recebida!",
            prestador.getUsuario().getNome() + " enviou uma proposta para: " + solicitacao.getDescricao().substring(0, Math.min(50, solicitacao.getDescricao().length()))
        );

        log.info("Proposta criada id={} para solicitacao={}", proposta.getId(), solicitacao.getId());
        return toDTO(proposta);
    }

    @Transactional(readOnly = true)
    public PropostaDTO buscarPorId(Long propostaId, Long usuarioId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));
        Long clienteId = proposta.getSolicitacao().getCliente().getId();
        Long prestadorUsuarioId = proposta.getPrestador().getUsuario().getId();
        if (!usuarioId.equals(clienteId) && !usuarioId.equals(prestadorUsuarioId)) {
            throw new BusinessException("Acesso negado");
        }
        return toDTO(proposta);
    }

    @Transactional(readOnly = true)
    public Page<PropostaDTO> listarPorSolicitacao(Long solicitacaoId, Pageable pageable) {
        return propostaRepository.findBySolicitacaoId(solicitacaoId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<PropostaDTO> listarPorPrestador(Long prestadorId, Pageable pageable) {
        return propostaRepository.findByPrestadorId(prestadorId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<PropostaDTO> listarMinhas(Long usuarioId, Pageable pageable) {
        Long prestadorId = prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador não encontrado"))
                .getId();
        return listarPorPrestador(prestadorId, pageable);
    }
    
    @Transactional
    public PropostaDTO aceitarProposta(Long propostaId, Long clienteId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));

        if (!proposta.getSolicitacao().getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Apenas o cliente dono da solicitação pode aceitar propostas");
        }

        if (proposta.getStatus() != StatusProposta.ENVIADA) {
            throw new BusinessException("Esta proposta já foi processada");
        }

        creditoService.consumirPorAceite(proposta);

        proposta.setStatus(StatusProposta.ACEITA);
        proposta.getSolicitacao().setStatus(StatusSolicitacao.EM_ANDAMENTO);

        proposta = propostaRepository.save(proposta);
        solicitacaoRepository.save(proposta.getSolicitacao());

        notificacaoService.criar(
            proposta.getPrestador().getUsuario().getId(),
            "Sua proposta foi aceita! Serviço: " + proposta.getSolicitacao().getDescricao().substring(0, Math.min(50, proposta.getSolicitacao().getDescricao().length())),
            "PROPOSTA_ACEITA",
            "/prestador/dashboard"
        );

        emailService.sendPropostaAceita(
            proposta.getPrestador().getUsuario().getEmail(),
            proposta.getSolicitacao().getCliente().getNome(),
            proposta.getSolicitacao().getDescricao()
        );

        webPushService.notificar(
            proposta.getPrestador().getUsuario().getId(),
            "Proposta aceita!",
            "Sua proposta foi aceita. Entre em contato pelo chat."
        );

        log.info("Proposta id={} aceita, notificacao enviada ao prestador", propostaId);
        return toDTO(proposta);
    }

    @Transactional
    public PropostaDTO recusarProposta(Long propostaId, Long clienteId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));

        if (!proposta.getSolicitacao().getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Apenas o cliente dono da solicitação pode recusar propostas");
        }

        if (proposta.getStatus() != StatusProposta.ENVIADA) {
            throw new BusinessException("Esta proposta já foi processada");
        }

        proposta.setStatus(StatusProposta.RECUSADA);
        proposta = propostaRepository.save(proposta);

        return toDTO(proposta);
    }

    @Transactional
    public PropostaDTO cancelarProposta(Long propostaId, Long usuarioId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));

        if (!proposta.getPrestador().getUsuario().getId().equals(usuarioId)) {
            throw new BusinessException("Apenas o prestador dono da proposta pode cancelá-la");
        }

        if (proposta.getStatus() != StatusProposta.ENVIADA) {
            throw new BusinessException("Só é possível cancelar propostas aguardando resposta");
        }

        proposta.setStatus(StatusProposta.CANCELADA_PELO_PRESTADOR);
        proposta = propostaRepository.save(proposta);

        log.info("Proposta id={} cancelada pelo prestador usuarioId={}", propostaId, usuarioId);
        return toDTO(proposta);
    }
    
    private PropostaDTO toDTO(Proposta proposta) {
        PropostaDTO dto = new PropostaDTO();
        dto.setId(proposta.getId());
        dto.setSolicitacaoId(proposta.getSolicitacao().getId());
        dto.setPrestadorId(proposta.getPrestador().getId());
        dto.setPrestadorNome(proposta.getPrestador().getUsuario().getNome());
        dto.setValor(proposta.getValor());
        dto.setMensagem(proposta.getMensagem());
        dto.setStatus(proposta.getStatus());
        dto.setDataEnvio(proposta.getDataEnvio());
        dto.setSolicitacaoDescricao(proposta.getSolicitacao().getDescricao());
        dto.setSolicitacaoStatus(proposta.getSolicitacao().getStatus());
        dto.setClienteNome(proposta.getSolicitacao().getCliente().getNome());
        dto.setClienteId(proposta.getSolicitacao().getCliente().getId());
        dto.setCidade(proposta.getSolicitacao().getCidade());
        dto.setEstado(proposta.getSolicitacao().getEstado());
        dto.setCategoriaNome(proposta.getSolicitacao().getCategoria().getNome());
        dto.setFoiAvaliadoPeloPrestador(
            avaliacaoRepository.existsBySolicitacaoIdAndTipo(proposta.getSolicitacao().getId(), TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE));
        return dto;
    }
}
