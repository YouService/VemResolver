package com.marketplace.servicos.repository;

import com.marketplace.servicos.model.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    List<MensagemChat> findByPropostaIdOrderByDataEnvioAsc(Long propostaId);

    Page<MensagemChat> findByPropostaIdOrderByDataEnvioDesc(Long propostaId, Pageable pageable);

    List<MensagemChat> findByPropostaIdAndRemetenteIdNotAndLidaFalse(Long propostaId, Long remetenteId);
}
