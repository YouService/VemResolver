package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.User;
import com.marketplace.servicos.model.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByIdAndTipo(Long id, TipoUsuario tipo);
}
