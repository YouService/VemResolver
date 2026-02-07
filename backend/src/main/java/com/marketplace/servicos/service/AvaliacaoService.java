package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.AvaliacaoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {
    
    private final AvaliacaoRepository avaliacaoRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final PrestadorRepository prestadorRepository;
    private final UserRepository userRepository;
    
    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                           SolicitacaoServicoRepository solicitacaoRepository,
                           PrestadorRepository prestadorRepository,
                           UserRepository userRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.prestadorRepository = prestadorRepository;
        this.userRepository = userRepository;
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
        
        if (avaliacaoRepository.existsBySolicitacaoId(solicitacao.getId())) {
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
        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());
        
        avaliacao = avaliacaoRepository.save(avaliacao);
        
        atualizarMediaPrestador(prestador);
        
        return toDTO(avaliacao);
    }
    
    public Page<AvaliacaoDTO> listarPorPrestador(Long prestadorId, Pageable pageable) {
        return avaliacaoRepository.findByPrestadorId(prestadorId, pageable)
                .map(this::toDTO);
    }
    
    private void atualizarMediaPrestador(Prestador prestador) {
        Page<Avaliacao> avaliacoes = avaliacaoRepository.findByPrestadorId(
                prestador.getId(), Pageable.unpaged());
        
        double media = avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);
        
        prestador.setAvaliacaoMedia(media);
        prestador.setTotalAvaliacoes((int) avaliacoes.getTotalElements());
        prestadorRepository.save(prestador);
    }
    
    private AvaliacaoDTO toDTO(Avaliacao avaliacao) {
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.setId(avaliacao.getId());
        dto.setSolicitacaoId(avaliacao.getSolicitacao().getId());
        dto.setPrestadorId(avaliacao.getPrestador().getId());
        dto.setClienteId(avaliacao.getCliente().getId());
        dto.setClienteNome(avaliacao.getCliente().getNome());
        dto.setNota(avaliacao.getNota());
        dto.setComentario(avaliacao.getComentario());
        return dto;
    }
}
