package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    @Query("""
        SELECT a FROM AdminAuditLog a
        WHERE (:acao IS NULL OR a.acao = :acao)
          AND (:adminId IS NULL OR a.admin.id = :adminId)
          AND (:de IS NULL OR a.criadoEm >= :de)
          AND (:ate IS NULL OR a.criadoEm <= :ate)
        ORDER BY a.criadoEm DESC
        """)
    Page<AdminAuditLog> filtrar(
            @Param("acao") String acao,
            @Param("adminId") Long adminId,
            @Param("de") LocalDateTime de,
            @Param("ate") LocalDateTime ate,
            Pageable pageable);
}
