package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.User;
import com.marketplace.servicos.model.TipoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByLogin(String login);
    Optional<User> findByIdAndTipo(Long id, TipoUsuario tipo);
    boolean existsByEmailAndAtivoTrue(String email);
    boolean existsByCpfAndAtivoTrue(String cpf);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.ultimoAcesso = :agora WHERE u.id = :id AND (u.ultimoAcesso IS NULL OR u.ultimoAcesso < :limiar)")
    int atualizarUltimoAcesso(@Param("id") Long id, @Param("agora") LocalDateTime agora, @Param("limiar") LocalDateTime limiar);

    @Query("SELECT u FROM User u WHERE u.ultimoAcesso > :limiar AND u.ativo = true ORDER BY u.ultimoAcesso DESC")
    List<User> findOnline(@Param("limiar") LocalDateTime limiar);

    long countByUltimoAcessoAfterAndAtivoTrue(LocalDateTime limiar);
}
