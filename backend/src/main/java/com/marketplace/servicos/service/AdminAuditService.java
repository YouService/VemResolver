package com.marketplace.servicos.service;

import com.marketplace.servicos.model.AdminAuditLog;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.AdminAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditService {

    private final AdminAuditLogRepository repository;

    public AdminAuditService(AdminAuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(User admin, String acao, String recursoTipo, Long recursoId,
                          String dadosAntes, String dadosDepois, String ip) {
        AdminAuditLog entry = new AdminAuditLog();
        entry.setAdmin(admin);
        entry.setAcao(acao);
        entry.setRecursoTipo(recursoTipo);
        entry.setRecursoId(recursoId);
        entry.setDadosAntes(dadosAntes);
        entry.setDadosDepois(dadosDepois);
        entry.setIpAddress(ip);
        entry.setStatus("SUCESSO");
        repository.save(entry);
    }
}
