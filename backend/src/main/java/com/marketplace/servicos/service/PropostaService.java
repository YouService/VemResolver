package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.PropostaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropostaService {
    
    private final PropostaRepository propostaRepository;
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final PrestadorRepository prestadorRepository;
    
    public PropostaService(PropostaRepository propostaRepository,
                          SolicitacaoServicoRepository solicitacaoRepository,
                          PrestadorRepository prestadorRepository) {
        this.propostaRepository = propostaRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.prestadorRepository = prestadorRepository;
    }
    
    @Transactional
    public PropostaDTO criar(PropostaDTO dto, Long prestadorId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        
        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new BusinessException("Esta solicitação não está mais aberta para propostas");
        }
        
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador não encontrado"));
        
        if (propostaRepository.existsBySolicitacaoIdAndPrestadorId(solicitacao.getId(), prestador.getId())) {
            throw new BusinessException("Você já enviou uma proposta para esta solicitação");
        }
        
        Proposta proposta = new Proposta();
        proposta.setSolicitacao(solicitacao);
        proposta.setPrestador(prestador);
        proposta.setValor(dto.getValor());
        proposta.setMensagem(dto.getMensagem());
        
        proposta = propostaRepository.save(proposta);
        return toDTO(proposta);
    }
    
    public Page<PropostaDTO> listarPorSolicitacao(Long solicitacaoId, Pageable pageable) {
        return propostaRepository.findBySolicitacaoId(solicitacaoId, pageable)
                .map(this::toDTO);
    }
    
    public Page<PropostaDTO> listarPorPrestador(Long prestadorId, Pageable pageable) {
        return propostaRepository.findByPrestadorId(prestadorId, pageable)
                .map(this::toDTO);
    }
    
    @Transactional
    public PropostaDTO aceitarProposta(Long propostaId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));
        
        if (proposta.getStatus() != StatusProposta.ENVIADA) {
            throw new BusinessException("Esta proposta já foi processada");
        }
        
        proposta.setStatus(StatusProposta.ACEITA);
        proposta.getSolicitacao().setStatus(StatusSolicitacao.EM_ANDAMENTO);
        
        proposta = propostaRepository.save(proposta);
        solicitacaoRepository.save(proposta.getSolicitacao());
        
        return toDTO(proposta);
    }
    
    @Transactional
    public PropostaDTO recusarProposta(Long propostaId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta não encontrada"));
        
        if (proposta.getStatus() != StatusProposta.ENVIADA) {
            throw new BusinessException("Esta proposta já foi processada");
        }
        
        proposta.setStatus(StatusProposta.RECUSADA);
        proposta = propostaRepository.save(proposta);
        
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
        return dto;
    }
}
