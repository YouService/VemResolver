package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findByUsuarioId(Long usuarioId);
    Optional<PushSubscription> findByUsuarioIdAndEndpoint(Long usuarioId, String endpoint);
    List<PushSubscription> findAllByUsuarioId(Long usuarioId);
}
