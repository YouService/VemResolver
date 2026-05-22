package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.StatusVerificacao;
import com.marketplace.servicos.model.VerificacaoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificacaoDocumentoRepository extends JpaRepository<VerificacaoDocumento, Long> {

    Optional<VerificacaoDocumento> findTopByPrestadorIdOrderByDataEnvioDesc(Long prestadorId);

    Page<VerificacaoDocumento> findByStatusOrderByDataEnvioAsc(StatusVerificacao status, Pageable pageable);

    Page<VerificacaoDocumento> findAllByOrderByDataEnvioDesc(Pageable pageable);

    long countByStatus(StatusVerificacao status);
}
