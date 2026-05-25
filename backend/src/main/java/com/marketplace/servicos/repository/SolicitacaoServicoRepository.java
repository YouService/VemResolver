package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.SolicitacaoServico;
import com.marketplace.servicos.model.StatusSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitacaoServicoRepository extends JpaRepository<SolicitacaoServico, Long> {
    Page<SolicitacaoServico> findByClienteId(Long clienteId, Pageable pageable);

    Page<SolicitacaoServico> findByStatus(StatusSolicitacao status, Pageable pageable);

    @Query("SELECT s FROM SolicitacaoServico s WHERE s.categoria.id = :categoriaId AND s.cidade = :cidade AND s.estado = :estado AND s.status = :status")
    Page<SolicitacaoServico> findByCategoriaAndLocalizacaoAndStatus(
        @Param("categoriaId") Long categoriaId,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        @Param("status") StatusSolicitacao status,
        Pageable pageable
    );

    @Query("SELECT s FROM SolicitacaoServico s WHERE s.categoria.id = :categoriaId AND s.status = :status")
    Page<SolicitacaoServico> findByCategoriaAndStatus(
        @Param("categoriaId") Long categoriaId,
        @Param("status") StatusSolicitacao status,
        Pageable pageable
    );

    @Query(value = """
        SELECT s.*,
               ts_rank(s.ts_descricao, plainto_tsquery('portuguese', :q)) AS rank
        FROM solicitacoes_servico s
        WHERE s.ts_descricao @@ plainto_tsquery('portuguese', :q)
          AND s.status = 'ABERTA'
          AND (:categoriaId IS NULL OR s.categoria_id = :categoriaId)
          AND (:cidade IS NULL OR s.cidade ILIKE :cidade)
          AND (:estado IS NULL OR s.estado = :estado)
        ORDER BY rank DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM solicitacoes_servico s
        WHERE s.ts_descricao @@ plainto_tsquery('portuguese', :q)
          AND s.status = 'ABERTA'
          AND (:categoriaId IS NULL OR s.categoria_id = :categoriaId)
          AND (:cidade IS NULL OR s.cidade ILIKE :cidade)
          AND (:estado IS NULL OR s.estado = :estado)
        """,
        nativeQuery = true)
    Page<SolicitacaoServico> buscarPorTexto(
        @Param("q") String q,
        @Param("categoriaId") Long categoriaId,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        Pageable pageable);
}
