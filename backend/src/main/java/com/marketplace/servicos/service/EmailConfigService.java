package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.ConfiguracaoEmailDTO;
import com.marketplace.servicos.model.ConfiguracaoEmail;
import com.marketplace.servicos.repository.ConfiguracaoEmailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailConfigService {

    private final ConfiguracaoEmailRepository repo;
    private final EmailService emailService;

    public EmailConfigService(ConfiguracaoEmailRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoEmailDTO buscar() {
        ConfiguracaoEmail cfg = repo.findFirstByOrderByIdAsc().orElseGet(ConfiguracaoEmail::new);
        return toDTO(cfg, false);
    }

    @Transactional
    public ConfiguracaoEmailDTO salvar(ConfiguracaoEmailDTO dto) {
        ConfiguracaoEmail cfg = repo.findFirstByOrderByIdAsc().orElseGet(ConfiguracaoEmail::new);

        if (dto.getBrevoApiKey() != null && !dto.getBrevoApiKey().isBlank()) {
            cfg.setBrevoApiKey(dto.getBrevoApiKey().trim());
        }
        if (dto.getEmailRemetente() != null) {
            cfg.setEmailRemetente(dto.getEmailRemetente().trim());
        }
        if (dto.getNomeRemetente() != null) {
            cfg.setNomeRemetente(dto.getNomeRemetente().trim());
        }
        if (dto.getAtivo() != null) {
            cfg.setAtivo(dto.getAtivo());
        }
        cfg.setAtualizadoEm(LocalDateTime.now());

        cfg = repo.save(cfg);
        return toDTO(cfg, false);
    }

    public void enviarTeste(String emailDestino) {
        emailService.sendEmailTeste(emailDestino);
    }

    private ConfiguracaoEmailDTO toDTO(ConfiguracaoEmail cfg, boolean exposeKey) {
        ConfiguracaoEmailDTO dto = new ConfiguracaoEmailDTO();
        dto.setBrevoApiKey(exposeKey ? cfg.getBrevoApiKey() : mask(cfg.getBrevoApiKey()));
        dto.setEmailRemetente(cfg.getEmailRemetente());
        dto.setNomeRemetente(cfg.getNomeRemetente());
        dto.setAtivo(cfg.getAtivo());
        dto.setAtualizadoEm(cfg.getAtualizadoEm());
        dto.setConfigurado(cfg.getBrevoApiKey() != null && !cfg.getBrevoApiKey().isBlank()
                && cfg.getEmailRemetente() != null && !cfg.getEmailRemetente().isBlank());
        return dto;
    }

    private String mask(String key) {
        if (key == null || key.length() < 8) return null;
        return key.substring(0, 4) + "••••••••" + key.substring(key.length() - 4);
    }
}
