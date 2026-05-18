package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Proposta;
import com.marketplace.servicos.model.StatusProposta;
import com.marketplace.servicos.model.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {
    Page<Proposta> findBySolicitacaoId(Long solicitacaoId, Pageable pageable);
    
    Page<Proposta> findByPrestadorId(Long prestadorId, Pageable pageable);
    
    Optional<Proposta> findBySolicitacaoIdAndPrestadorId(Long solicitacaoId, Long prestadorId);
    
    boolean existsBySolicitacaoIdAndPrestadorId(Long solicitacaoId, Long prestadorId);
    
    Page<Proposta> findByPrestadorIdAndStatus(Long prestadorId, StatusProposta status, Pageable pageable);

    Optional<Proposta> findBySolicitacaoIdAndStatus(Long solicitacaoId, StatusProposta status);

    void deleteAllBySolicitacaoId(Long solicitacaoId);

    @Query("SELECT COUNT(p) FROM Proposta p WHERE p.prestador.id = :prestadorId AND p.status = :statusProposta AND p.solicitacao.status = :statusSolicitacao")
    long countByPrestadorIdAndStatusAndSolicitacaoStatus(
            @Param("prestadorId") Long prestadorId,
            @Param("statusProposta") StatusProposta statusProposta,
            @Param("statusSolicitacao") StatusSolicitacao statusSolicitacao);

    long countBySolicitacaoId(Long solicitacaoId);
}
