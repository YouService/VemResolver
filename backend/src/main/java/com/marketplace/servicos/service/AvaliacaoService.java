package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AvaliacaoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AvaliacaoService {

    private static final Logger log = LoggerFactory.getLogger(AvaliacaoService.class);

    private final AvaliacaoRepository avaliacaoRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final PrestadorRepository prestadorRepository;
    private final UserRepository userRepository;
    private final PropostaRepository propostaRepository;
    private final NotificacaoService notificacaoService;
    private final PrestadorService prestadorService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            SolicitacaoServicoRepository solicitacaoRepository,
                            PrestadorRepository prestadorRepository,
                            UserRepository userRepository,
                            PropostaRepository propostaRepository,
                            NotificacaoService notificacaoService,
                            PrestadorService prestadorService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.prestadorRepository = prestadorRepository;
        this.userRepository = userRepository;
        this.propostaRepository = propostaRepository;
        this.notificacaoService = notificacaoService;
        this.prestadorService = prestadorService;
    }

    @Transactional
    public AvaliacaoDTO criar(AvaliacaoDTO dto, Long clienteId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusSolicitacao.CONCLUIDA) {
            throw new BusinessException("Só é possível avaliar solicitações concluídas");
        }

        if (!solicitacao.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Você não pode avaliar esta solicitação");
        }

        if (avaliacaoRepository.existsBySolicitacaoIdAndTipo(solicitacao.getId(), TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR)) {
            throw new BusinessException("Esta solicitação já foi avaliada");
        }

        Prestador prestador = prestadorRepository.findById(dto.getPrestadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Prestador não encontrado"));

        User cliente = userRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setSolicitacao(solicitacao);
        avaliacao.setPrestador(prestador);
        avaliacao.setCliente(cliente);
        avaliacao.setTipo(TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());

        avaliacao = avaliacaoRepository.save(avaliacao);
        atualizarMediaPrestador(prestador);
        prestadorService.recalcularVerificado(prestador.getId());

        notificacaoService.criar(
            avaliacao.getPrestador().getUsuario().getId(),
            "Você recebeu uma avaliação de " + avaliacao.getNota() + " estrelas!",
            "AVALIACAO_RECEBIDA",
            "/prestador/dashboard"
        );

        log.info("Avaliacao id={} criada pelo cliente id={}", avaliacao.getId(), clienteId);
        return toDTO(avaliacao);
    }

    @Transactional
    public AvaliacaoDTO criarPorPrestador(AvaliacaoDTO dto, Long prestadorUserId) {
        Prestador prestador = prestadorRepository.findByUsuarioId(prestadorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de prestador não encontrado"));

        SolicitacaoServico solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (solicitacao.getStatus() != StatusSolicitacao.CONCLUIDA) {
            throw new BusinessException("Só é possível avaliar solicitações concluídas");
        }

        propostaRepository.findBySolicitacaoIdAndPrestadorId(solicitacao.getId(), prestador.getId())
                .filter(p -> p.getStatus() == StatusProposta.ACEITA)
                .orElseThrow(() -> new BusinessException("Você não prestou este serviço"));

        if (avaliacaoRepository.existsBySolicitacaoIdAndTipo(solicitacao.getId(), TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE)) {
            throw new BusinessException("Você já avaliou o cliente desta solicitação");
        }

        User cliente = solicitacao.getCliente();

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setSolicitacao(solicitacao);
        avaliacao.setPrestador(prestador);
        avaliacao.setCliente(cliente);
        avaliacao.setTipo(TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE);
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());

        avaliacao = avaliacaoRepository.save(avaliacao);
        atualizarMediaCliente(cliente);

        notificacaoService.criar(
            avaliacao.getCliente().getId(),
            "Você recebeu uma avaliação de " + avaliacao.getNota() + " estrelas do prestador!",
            "AVALIACAO_RECEBIDA",
            "/cliente/dashboard"
        );

        log.info("Avaliacao id={} criada pelo prestador usuario id={}", avaliacao.getId(), prestadorUserId);
        return toDTO(avaliacao);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoDTO> listarPorPrestador(Long prestadorId, Pageable pageable) {
        return avaliacaoRepository.findByPrestadorIdAndTipo(prestadorId, TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoDTO> listarPorCliente(Long clienteId, Pageable pageable) {
        return avaliacaoRepository.findByClienteIdAndTipo(clienteId, TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE, pageable)
                .map(this::toDTO);
    }

    private void atualizarMediaPrestador(Prestador prestador) {
        Page<Avaliacao> pagina = avaliacaoRepository.findByPrestadorIdAndTipo(
                prestador.getId(), TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR, Pageable.unpaged());
        List<Avaliacao> avaliacoes = pagina.getContent();

        double media = avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        prestador.setAvaliacaoMedia(media);
        prestador.setTotalAvaliacoes((int) pagina.getTotalElements());

        prestador.setMediaPontualidade(calcularMediaDimensao(avaliacoes, Avaliacao::getNotaPontualidade));
        prestador.setMediaQualidade(calcularMediaDimensao(avaliacoes, Avaliacao::getNotaQualidade));
        prestador.setMediaComunicacao(calcularMediaDimensao(avaliacoes, Avaliacao::getNotaComunicacao));

        prestadorRepository.save(prestador);
    }

    private Double calcularMediaDimensao(List<Avaliacao> avaliacoes,
                                          java.util.function.Function<Avaliacao, Integer> extrator) {
        return avaliacoes.stream()
                .map(extrator)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
    }

    private void atualizarMediaCliente(User cliente) {
        Page<Avaliacao> avaliacoes = avaliacaoRepository.findByClienteIdAndTipo(
                cliente.getId(), TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE, Pageable.unpaged());

        double media = avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        cliente.setAvaliacaoMedia(media);
        cliente.setTotalAvaliacoes((int) avaliacoes.getTotalElements());
        userRepository.save(cliente);
    }

    private AvaliacaoDTO toDTO(Avaliacao avaliacao) {
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.setId(avaliacao.getId());
        dto.setSolicitacaoId(avaliacao.getSolicitacao().getId());
        dto.setPrestadorId(avaliacao.getPrestador().getId());
        dto.setClienteId(avaliacao.getCliente().getId());
        dto.setClienteNome(avaliacao.getCliente().getNome());
        dto.setTipo(avaliacao.getTipo());
        dto.setNota(avaliacao.getNota());
        dto.setComentario(avaliacao.getComentario());
        dto.setDataCriacao(avaliacao.getDataCriacao());

        if (avaliacao.getTipo() == TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR) {
            dto.setNomeAvaliador(avaliacao.getCliente().getNome());
            dto.setNomeAvaliado(avaliacao.getPrestador().getUsuario().getNome());
        } else {
            dto.setNomeAvaliador(avaliacao.getPrestador().getUsuario().getNome());
            dto.setNomeAvaliado(avaliacao.getCliente().getNome());
        }

        return dto;
    }
}
