package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Avaliacao;
import com.marketplace.servicos.model.TipoAvaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Page<Avaliacao> findByPrestadorIdAndTipo(Long prestadorId, TipoAvaliacao tipo, Pageable pageable);

    Page<Avaliacao> findByClienteIdAndTipo(Long clienteId, TipoAvaliacao tipo, Pageable pageable);

    boolean existsBySolicitacaoIdAndTipo(Long solicitacaoId, TipoAvaliacao tipo);

    List<Avaliacao> findTop5ByPrestadorIdAndTipoOrderByDataCriacaoDesc(Long prestadorId, TipoAvaliacao tipo);

    @Query("""
        SELECT a FROM Avaliacao a
        WHERE a.prestador.id = :prestadorId
          AND a.tipo = com.marketplace.servicos.model.TipoAvaliacao.CLIENTE_AVALIA_PRESTADOR
          AND (:nota IS NULL OR a.nota = :nota)
        ORDER BY a.dataCriacao DESC
        """)
    Page<Avaliacao> filtrarPorNota(@Param("prestadorId") Long prestadorId,
                                   @Param("nota") Integer nota,
                                   Pageable pageable);
}
