package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.TransacaoCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransacaoCreditoRepository extends JpaRepository<TransacaoCredito, Long> {

    Page<TransacaoCredito> findByContaIdOrderByDataCriacaoDescIdDesc(Long contaId, Pageable pageable);

    Optional<TransacaoCredito> findByReferenciaExterna(String referenciaExterna);
}
