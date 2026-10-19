package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.ContaCredito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContaCreditoRepository extends JpaRepository<ContaCredito, Long> {

    Optional<ContaCredito> findByPrestadorId(Long prestadorId);

    // Bloqueia a linha para evitar débito duplo em aceites simultâneos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaCredito c WHERE c.prestador.id = :prestadorId")
    Optional<ContaCredito> findByPrestadorIdParaAtualizacao(@Param("prestadorId") Long prestadorId);
}
