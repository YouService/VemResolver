package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.PacoteCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PacoteCreditoRepository extends JpaRepository<PacoteCredito, Long> {

    List<PacoteCredito> findByAtivoTrueOrderByPrecoCentavosAsc();

    List<PacoteCredito> findAllByOrderByPrecoCentavosAsc();
}
