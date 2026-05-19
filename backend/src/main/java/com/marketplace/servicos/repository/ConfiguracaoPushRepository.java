package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.ConfiguracaoPush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoPushRepository extends JpaRepository<ConfiguracaoPush, Long> {
    Optional<ConfiguracaoPush> findFirstByOrderByIdAsc();
}
