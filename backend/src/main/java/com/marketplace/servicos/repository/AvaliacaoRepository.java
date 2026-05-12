package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Avaliacao;
import com.marketplace.servicos.model.TipoAvaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    Page<Avaliacao> findByPrestadorIdAndTipo(Long prestadorId, TipoAvaliacao tipo, Pageable pageable);

    Page<Avaliacao> findByClienteIdAndTipo(Long clienteId, TipoAvaliacao tipo, Pageable pageable);

    boolean existsBySolicitacaoIdAndTipo(Long solicitacaoId, TipoAvaliacao tipo);
}
