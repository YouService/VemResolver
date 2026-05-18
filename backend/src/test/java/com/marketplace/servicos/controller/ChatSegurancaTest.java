package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.ChatService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = ChatController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class ChatSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;
    @MockBean private ChatService chatService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private User buildUser(TipoUsuario tipo) {
        User u = new User();
        u.setId(1L);
        u.setNome("Test");
        u.setEmail("test@test.com");
        u.setLogin("test");
        u.setSenha("pass");
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setAvaliacaoMedia(0.0);
        u.setTotalAvaliacoes(0);
        return u;
    }

    // ── GET /api/chat ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/chat/proposta/1 — sem autenticação → 403")
    void listarMensagens_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/chat/proposta/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/chat/proposta/1 — como CLIENTE → acesso permitido (não 403)")
    void listarMensagens_ComoCliente_NaoRetorna403() throws Exception {
        when(chatService.listarMensagens(eq(1L), anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/chat/proposta/1")
                        .with(user(buildUser(TipoUsuario.CLIENTE))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/chat/proposta/1 — como PRESTADOR → acesso permitido (não 403)")
    void listarMensagens_ComoPrestador_NaoRetorna403() throws Exception {
        when(chatService.listarMensagens(eq(1L), anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/chat/proposta/1")
                        .with(user(buildUser(TipoUsuario.PRESTADOR))))
                .andExpect(status().isOk());
    }

    // ── POST /api/chat ────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/chat/proposta/1 — sem autenticação → 403")
    void enviarMensagem_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(post("/api/chat/proposta/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"oi\",\"propostaId\":1}"))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/chat ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /api/chat/proposta/1/ler — sem autenticação → 403")
    void marcarLidas_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/chat/proposta/1/ler").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/chat/proposta/1/ler — como CLIENTE → acesso permitido (não 403)")
    void marcarLidas_ComoCliente_NaoRetorna403() throws Exception {
        doNothing().when(chatService).marcarComoLidas(eq(1L), anyLong());

        mockMvc.perform(patch("/api/chat/proposta/1/ler")
                        .with(csrf())
                        .with(user(buildUser(TipoUsuario.CLIENTE))))
                .andExpect(status().isNoContent());
    }
}
