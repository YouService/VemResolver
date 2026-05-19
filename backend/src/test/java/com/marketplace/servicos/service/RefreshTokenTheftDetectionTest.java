package com.marketplace.servicos.service;

import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.model.RefreshToken;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenTheftDetectionTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        // 30 dias em ms
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 2592000000L);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
    }

    // ── generateRefreshToken ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateRefreshToken: deleta tokens antigos e cria novo com 30 dias de TTL")
    void generateRefreshToken_DeletaAntigos_CriaNovoComTtl30Dias() {
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken token = refreshTokenService.generateRefreshToken(user);

        verify(refreshTokenRepository).deleteByUser(user);
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getRevoked()).isFalse();
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now().plusDays(29));
    }

    // ── validateAndRotate: token válido ─────────────────────────────────────

    @Test
    @DisplayName("validateAndRotate: token válido → revoga antigo, emite novo")
    void validateAndRotate_TokenValido_RevogaAntigoEmiteNovo() {
        RefreshToken existing = buildToken(false, LocalDateTime.now().plusDays(25));
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken novo = refreshTokenService.validateAndRotate("valid-token");

        // Token antigo marcado como revogado
        assertThat(existing.getRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any()); // existing + novo

        // Novo token está ativo
        assertThat(novo.getRevoked()).isFalse();
        assertThat(novo.getUser()).isEqualTo(user);
    }

    // ── validateAndRotate: token já revogado → detecção de roubo ────────────

    @Test
    @DisplayName("validateAndRotate: token revogado → deleta todos os tokens do usuário e lança exceção de atividade suspeita")
    void validateAndRotate_TokenRevogado_DetectaRouboERevogaTodos() {
        RefreshToken revoked = buildToken(true, LocalDateTime.now().plusDays(20));
        when(refreshTokenRepository.findByToken("stolen-token")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> refreshTokenService.validateAndRotate("stolen-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("suspeita");

        // Revoga TODOS os tokens do usuário comprometido
        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository, never()).save(any());
    }

    // ── validateAndRotate: token expirado ────────────────────────────────────

    @Test
    @DisplayName("validateAndRotate: token expirado → deleta e lança exceção de expirado")
    void validateAndRotate_TokenExpirado_LancaExcecaoDeExpirado() {
        RefreshToken expired = buildToken(false, LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.validateAndRotate("expired-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expirado");

        verify(refreshTokenRepository).delete(expired);
        verify(refreshTokenRepository, never()).save(any());
    }

    // ── validateAndRotate: token inexistente ─────────────────────────────────

    @Test
    @DisplayName("validateAndRotate: token não encontrado → lança exceção de inválido")
    void validateAndRotate_TokenInexistente_LancaExcecaoDeInvalido() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.validateAndRotate("unknown"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inválido");
    }

    // ── revokeByUser ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("revokeByUser: delega ao repositório deleteByUser")
    void revokeByUser_DelegaAoRepositorio() {
        refreshTokenService.revokeByUser(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    private RefreshToken buildToken(boolean revoked, LocalDateTime expiryDate) {
        RefreshToken rt = new RefreshToken();
        rt.setToken("test-token");
        rt.setUser(user);
        rt.setRevoked(revoked);
        rt.setExpiryDate(expiryDate);
        return rt;
    }
}
