package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.FotoPrestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotoPrestadorRepository extends JpaRepository<FotoPrestador, Long> {
    List<FotoPrestador> findByPrestadorIdOrderByDataUploadAsc(Long prestadorId);
    long countByPrestadorId(Long prestadorId);
}
