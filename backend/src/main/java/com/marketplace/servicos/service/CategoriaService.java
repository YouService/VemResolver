package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.CategoriaDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.Categoria;
import com.marketplace.servicos.repository.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {
    
    private final CategoriaRepository categoriaRepository;
    
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }
    
    public Page<CategoriaDTO> listarTodas(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
                .map(this::toDTO);
    }
    
    public CategoriaDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        return toDTO(categoria);
    }
    
    @Transactional
    public CategoriaDTO criar(CategoriaDTO dto) {
        if (categoriaRepository.existsByNome(dto.getNome())) {
            throw new BusinessException("Já existe uma categoria com este nome");
        }
        
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        
        categoria = categoriaRepository.save(categoria);
        return toDTO(categoria);
    }
    
    @Transactional
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
        
        if (!categoria.getNome().equals(dto.getNome()) && categoriaRepository.existsByNome(dto.getNome())) {
            throw new BusinessException("Já existe uma categoria com este nome");
        }
        
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        
        categoria = categoriaRepository.save(categoria);
        return toDTO(categoria);
    }
    
    @Transactional
    public void deletar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada");
        }
        categoriaRepository.deleteById(id);
    }
    
    private CategoriaDTO toDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao()
        );
    }
}
