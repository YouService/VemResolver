package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.AvaliacaoRepository;
import com.marketplace.servicos.repository.CategoriaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitacaoServicoService {
    
    private final SolicitacaoServicoRepository solicitacaoRepository;
    private final UserRepository userRepository;
    private final CategoriaRepository categoriaRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public SolicitacaoServicoService(SolicitacaoServicoRepository solicitacaoRepository,
                                     UserRepository userRepository,
                                     CategoriaRepository categoriaRepository,
                                     AvaliacaoRepository avaliacaoRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.userRepository = userRepository;
        this.categoriaRepository = categoriaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }
    
    @Transactional
    public SolicitacaoServicoDTO criar(SolicitacaoServicoDTO dto, Long clienteId) {
        User cliente = userRepository.findByIdAndTipo(clienteId, TipoUsuario.CLIENTE)
                .orElseThrow(() -> new BusinessException("Apenas clientes podem criar solicitações"));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        SolicitacaoServico solicitacao = new SolicitacaoServico();
        solicitacao.setCliente(cliente);
        solicitacao.setCategoria(categoria);
        solicitacao.setDescricao(dto.getDescricao());
        solicitacao.setCidade(dto.getCidade());
        solicitacao.setEstado(dto.getEstado());

        solicitacao = solicitacaoRepository.save(solicitacao);
        return toDTO(solicitacao);
    }

    @Transactional
    public SolicitacaoServicoDTO atualizar(Long id, SolicitacaoServicoDTO dto, Long clienteId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!solicitacao.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Você não tem permissão para editar esta solicitação");
        }

        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new BusinessException("Apenas solicitações abertas podem ser editadas");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

        solicitacao.setCategoria(categoria);
        solicitacao.setDescricao(dto.getDescricao());
        solicitacao.setCidade(dto.getCidade());
        solicitacao.setEstado(dto.getEstado());

        solicitacao = solicitacaoRepository.save(solicitacao);
        return toDTO(solicitacao);
    }
    
    public SolicitacaoServicoDTO buscarPorId(Long id) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));
        return toDTO(solicitacao);
    }
    
    public Page<SolicitacaoServicoDTO> listarPorCliente(Long clienteId, Pageable pageable) {
        return solicitacaoRepository.findByClienteId(clienteId, pageable)
                .map(this::toDTO);
    }
    
    public Page<SolicitacaoServicoDTO> buscarAbertas(Long categoriaId, String cidade, String estado, Pageable pageable) {
        Page<SolicitacaoServico> solicitacoes;
        
        if (categoriaId != null && cidade != null && estado != null) {
            solicitacoes = solicitacaoRepository.findByCategoriaAndLocalizacaoAndStatus(
                    categoriaId, cidade, estado, StatusSolicitacao.ABERTA, pageable);
        } else if (categoriaId != null) {
            solicitacoes = solicitacaoRepository.findByCategoriaAndStatus(
                    categoriaId, StatusSolicitacao.ABERTA, pageable);
        } else {
            solicitacoes = solicitacaoRepository.findByStatus(StatusSolicitacao.ABERTA, pageable);
        }
        
        return solicitacoes.map(this::toDTO);
    }
    
    @Transactional
    public SolicitacaoServicoDTO atualizarStatus(Long id, StatusSolicitacao novoStatus) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        solicitacao.setStatus(novoStatus);
        solicitacao = solicitacaoRepository.save(solicitacao);
        return toDTO(solicitacao);
    }

    @Transactional
    public SolicitacaoServicoDTO concluirServico(Long id, Long clienteId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!solicitacao.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Apenas o cliente responsável pode concluir a solicitação");
        }

        if (solicitacao.getStatus() != StatusSolicitacao.EM_ANDAMENTO) {
            throw new BusinessException("Apenas solicitações em andamento podem ser concluídas");
        }

        solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
        solicitacao = solicitacaoRepository.save(solicitacao);
        return toDTO(solicitacao);
    }

    @Transactional
    public void deletar(Long id, Long clienteId) {
        SolicitacaoServico solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!solicitacao.getCliente().getId().equals(clienteId)) {
            throw new BusinessException("Você não tem permissão para excluir esta solicitação");
        }

        if (solicitacao.getStatus() != StatusSolicitacao.ABERTA) {
            throw new BusinessException("Apenas solicitações abertas podem ser excluídas");
        }

        solicitacaoRepository.delete(solicitacao);
    }

    private SolicitacaoServicoDTO toDTO(SolicitacaoServico solicitacao) {
        SolicitacaoServicoDTO dto = new SolicitacaoServicoDTO();
        dto.setId(solicitacao.getId());
        dto.setClienteId(solicitacao.getCliente().getId());
        dto.setClienteNome(solicitacao.getCliente().getNome());
        dto.setCategoriaId(solicitacao.getCategoria().getId());
        dto.setCategoriaNome(solicitacao.getCategoria().getNome());
        dto.setDescricao(solicitacao.getDescricao());
        dto.setCidade(solicitacao.getCidade());
        dto.setEstado(solicitacao.getEstado());
        dto.setStatus(solicitacao.getStatus());
        dto.setDataCriacao(solicitacao.getDataCriacao());
        dto.setFoiAvaliadoPeloCliente(
            avaliacaoRepository.existsBySolicitacaoIdAndTipo(solicitacao.getId(), TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR));
        dto.setFoiAvaliadoPeloPrestador(
            avaliacaoRepository.existsBySolicitacaoIdAndTipo(solicitacao.getId(), TipoAvaliacao.PRESTADOR_AVALIA_CLIENTE));
        return dto;
    }
}
