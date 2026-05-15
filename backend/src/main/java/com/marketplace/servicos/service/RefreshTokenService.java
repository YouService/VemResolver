package com.marketplace.servicos.service;

import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.RefreshToken;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        token.setRevoked(false);
        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken validateAndRotate(String tokenString) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (existing.getRevoked() || existing.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(existing);
            throw new BusinessException("Refresh token expirado. Faça login novamente.");
        }

        User user = existing.getUser();
        refreshTokenRepository.deleteByUser(user);

        RefreshToken newToken = new RefreshToken();
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setUser(user);
        newToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        newToken.setRevoked(false);
        return refreshTokenRepository.save(newToken);
    }

    @Transactional
    public void revokeByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
