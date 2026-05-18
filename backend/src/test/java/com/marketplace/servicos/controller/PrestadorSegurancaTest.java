package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.FotoPrestadorService;
import com.marketplace.servicos.service.PrestadorService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = {PrestadorController.class, FotoPrestadorController.class},
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class PrestadorSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;
    @MockBean private PrestadorService prestadorService;
    @MockBean private FotoPrestadorService fotoPrestadorService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // ── GET públicos ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/prestadores — público → 200")
    void listarPrestadores_SemAutenticacao_Retorna200() throws Exception {
        mockMvc.perform(get("/api/prestadores"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/prestadores/{id} — público → 200")
    void buscarPorId_SemAutenticacao_Retorna200() throws Exception {
        mockMvc.perform(get("/api/prestadores/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/prestadores/{id}/publico — público → 200")
    void perfilPublico_SemAutenticacao_Retorna200() throws Exception {
        mockMvc.perform(get("/api/prestadores/1/publico"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/prestadores/{id}/fotos — público → 200")
    void listarFotos_SemAutenticacao_Retorna200() throws Exception {
        mockMvc.perform(get("/api/prestadores/1/fotos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/prestadores/proximo — público → 200")
    void buscarProximo_SemAutenticacao_Retorna200() throws Exception {
        mockMvc.perform(get("/api/prestadores/proximo").param("lat", "-23.5").param("lng", "-46.6").param("raioKm", "20"))
                .andExpect(status().isOk());
    }

    // ── GET autenticados ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/prestadores/me/completude — sem autenticação → 403")
    void completude_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/prestadores/me/completude"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/prestadores/me/completude — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void completude_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/prestadores/me/completude"))
                .andExpect(status().isForbidden());
    }

    // ── POST — requer PRESTADOR ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/prestadores — sem autenticação → 403")
    void criarPrestador_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(post("/api/prestadores")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/prestadores — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void criarPrestador_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(post("/api/prestadores")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/prestadores/me/fotos — sem autenticação → 403")
    void uploadFoto_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(multipart("/api/prestadores/me/fotos")
                        .file("file", new byte[]{1, 2, 3}))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/prestadores/me/fotos — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void uploadFoto_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(multipart("/api/prestadores/me/fotos")
                        .file("file", new byte[]{1, 2, 3}))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/prestadores/me/foto-perfil — sem autenticação → 403")
    void uploadAvatar_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(multipart("/api/prestadores/me/foto-perfil")
                        .file("file", new byte[]{1, 2, 3}))
                .andExpect(status().isForbidden());
    }

    // ── DELETE — requer PRESTADOR ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/prestadores/me/fotos/1 — sem autenticação → 403")
    void deletarFoto_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/prestadores/me/fotos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/prestadores/me/fotos/1 — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void deletarFoto_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/prestadores/me/fotos/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/prestadores/me/foto-perfil — sem autenticação → 403")
    void deletarAvatar_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/prestadores/me/foto-perfil"))
                .andExpect(status().isForbidden());
    }
}
