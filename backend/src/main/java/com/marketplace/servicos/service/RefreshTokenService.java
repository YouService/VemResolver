package com.marketplace.servicos.service;

import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.RefreshToken;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    // 30 dias em milissegundos
    @Value("${jwt.refresh-expiration:2592000000}")
    private Long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        return createToken(user);
    }

    @Transactional
    public RefreshToken validateAndRotate(String tokenString) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (existing.getRevoked()) {
            // Token já foi rotacionado: possível roubo — invalida toda a família
            User user = existing.getUser();
            log.warn("Reutilização de refresh token detectada para userId={}. Revogando todos os tokens.", user.getId());
            refreshTokenRepository.deleteByUser(user);
            throw new BusinessException("Atividade suspeita detectada. Faça login novamente.");
        }

        if (existing.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(existing);
            throw new BusinessException("Refresh token expirado. Faça login novamente.");
        }

        // Marca o token atual como revogado (mantém no BD para detecção de roubo)
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createToken(existing.getUser());
    }

    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private RefreshToken createToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }
}
