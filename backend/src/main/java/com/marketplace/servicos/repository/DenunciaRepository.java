package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.Denuncia;
import com.marketplace.servicos.model.StatusDenuncia;
import com.marketplace.servicos.model.TipoDenuncia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    boolean existsByDenuncianteIdAndDenunciadoIdAndTipo(Long denuncianteId, Long denunciadoId, TipoDenuncia tipo);

    long countByDenunciadoIdAndStatus(Long denunciadoId, StatusDenuncia status);

    @Query("SELECT d FROM Denuncia d WHERE (:status IS NULL OR d.status = :status) ORDER BY d.dataCriacao DESC")
    Page<Denuncia> filtrar(@Param("status") StatusDenuncia status, Pageable pageable);

    long countByStatus(StatusDenuncia status);
}
