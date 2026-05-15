package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    List<MensagemChat> findBySolicitacaoIdOrderByDataEnvioAsc(Long solicitacaoId);

    List<MensagemChat> findBySolicitacaoIdAndRemetenteIdNotAndLidaFalse(Long solicitacaoId, Long remetenteId);
}
