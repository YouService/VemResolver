package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    List<MensagemChat> findByPropostaIdOrderByDataEnvioAsc(Long propostaId);

    List<MensagemChat> findByPropostaIdAndRemetenteIdNotAndLidaFalse(Long propostaId, Long remetenteId);
}
