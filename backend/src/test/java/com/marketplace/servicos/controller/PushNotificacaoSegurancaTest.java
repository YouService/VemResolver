package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.WebPushService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = PushNotificacaoController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class PushNotificacaoSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;
    @MockBean private WebPushService webPushService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private User buildUser() {
        User u = new User();
        u.setId(1L);
        u.setNome("Test");
        u.setEmail("test@test.com");
        u.setLogin("test");
        u.setSenha("pass");
        u.setTipo(TipoUsuario.CLIENTE);
        u.setAtivo(true);
        u.setAvaliacaoMedia(0.0);
        u.setTotalAvaliacoes(0);
        return u;
    }

    // PN-01: GET /api/notificacoes/push/public-key sem auth → 403
    @Test
    @DisplayName("PN-01: GET /api/notificacoes/push/public-key — sem autenticação → 403")
    void getPublicKey_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/notificacoes/push/public-key"))
                .andExpect(status().isForbidden());
    }

    // PN-02: GET /api/notificacoes/push/public-key com auth → 200
    @Test
    @DisplayName("PN-02: GET /api/notificacoes/push/public-key — com autenticação → 200")
    void getPublicKey_ComAutenticacao_Retorna200() throws Exception {
        when(webPushService.getPublicKey()).thenReturn("chave-publica-vapid");

        mockMvc.perform(get("/api/notificacoes/push/public-key")
                        .with(user(buildUser())))
                .andExpect(status().isOk());
    }

    // PN-03: POST /api/notificacoes/push/subscribe sem auth → 403
    @Test
    @DisplayName("PN-03: POST /api/notificacoes/push/subscribe — sem autenticação → 403")
    void subscribe_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(post("/api/notificacoes/push/subscribe")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpoint\":\"https://push.example.com\",\"p256dh\":\"chave\",\"auth\":\"auth\"}"))
                .andExpect(status().isForbidden());
    }

    // PN-04: POST /api/notificacoes/push/subscribe com auth → 204
    @Test
    @DisplayName("PN-04: POST /api/notificacoes/push/subscribe — com autenticação → 204")
    void subscribe_ComAutenticacao_Retorna204() throws Exception {
        mockMvc.perform(post("/api/notificacoes/push/subscribe")
                        .with(csrf())
                        .with(user(buildUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpoint\":\"https://push.example.com\",\"p256dh\":\"chave\",\"auth\":\"auth\"}"))
                .andExpect(status().isNoContent());
    }

    // PN-05: DELETE /api/notificacoes/push/unsubscribe sem auth → 403
    @Test
    @DisplayName("PN-05: DELETE /api/notificacoes/push/unsubscribe — sem autenticação → 403")
    void unsubscribe_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/notificacoes/push/unsubscribe")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endpoint\":\"https://push.example.com\",\"p256dh\":\"chave\",\"auth\":\"auth\"}"))
                .andExpect(status().isForbidden());
    }
}
