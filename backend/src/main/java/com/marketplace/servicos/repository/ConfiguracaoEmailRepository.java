package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.ConfiguracaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoEmailRepository extends JpaRepository<ConfiguracaoEmail, Long> {
    Optional<ConfiguracaoEmail> findFirstByOrderByIdAsc();
}
