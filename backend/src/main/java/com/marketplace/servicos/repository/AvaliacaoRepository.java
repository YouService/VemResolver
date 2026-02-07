package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Page<Avaliacao> findByPrestadorId(Long prestadorId, Pageable pageable);
    
    Optional<Avaliacao> findBySolicitacaoId(Long solicitacaoId);
    
    boolean existsBySolicitacaoId(Long solicitacaoId);
}
