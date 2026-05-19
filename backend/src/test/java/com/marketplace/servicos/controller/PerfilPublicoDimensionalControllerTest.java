package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.PerfilPublicoDTO;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = {PrestadorController.class, FotoPrestadorController.class},
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class PerfilPublicoDimensionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private PrestadorService prestadorService;
    @MockBean private FotoPrestadorService fotoPrestadorService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private PerfilPublicoDTO buildPerfilDTO(Double pontualidade, Double qualidade, Double comunicacao) {
        return new PerfilPublicoDTO(
                10L, "Maria Silva", null,
                List.of("Eletricista"), "São Paulo", "SP",
                4.5, 12, true,
                pontualidade, qualidade, comunicacao,
                List.of(), List.of()
        );
    }

    // ── GET com todas as médias dimensionais ─────────────────────────────────

    @Test
    @DisplayName("GET /api/prestadores/10/publico com médias dimensionais → campos no JSON")
    void perfilPublico_ComTodasMediasDimensionais_RetornaNoJson() throws Exception {
        when(prestadorService.buscarPerfilPublico(10L))
                .thenReturn(buildPerfilDTO(4.8, 4.5, 4.2));

        mockMvc.perform(get("/api/prestadores/10/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Maria Silva"))
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.mediaPontualidade").value(4.8))
                .andExpect(jsonPath("$.mediaQualidade").value(4.5))
                .andExpect(jsonPath("$.mediaComunicacao").value(4.2));
    }

    // ── GET sem médias dimensionais → campos ausentes ─────────────────────────

    @Test
    @DisplayName("GET /api/prestadores/10/publico sem médias dimensionais → campos ausentes no JSON")
    void perfilPublico_SemMediasDimensionais_CamposAusentesNoJson() throws Exception {
        when(prestadorService.buscarPerfilPublico(10L))
                .thenReturn(buildPerfilDTO(null, null, null));

        mockMvc.perform(get("/api/prestadores/10/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaPontualidade").doesNotExist())
                .andExpect(jsonPath("$.mediaQualidade").doesNotExist())
                .andExpect(jsonPath("$.mediaComunicacao").doesNotExist());
    }

    // ── GET com apenas uma dimensão definida ─────────────────────────────────

    @Test
    @DisplayName("GET /api/prestadores/10/publico com apenas mediaPontualidade → somente ela no JSON")
    void perfilPublico_SomenteMediaPontualidade_RetornaApenasPontualidade() throws Exception {
        when(prestadorService.buscarPerfilPublico(10L))
                .thenReturn(buildPerfilDTO(4.8, null, null));

        mockMvc.perform(get("/api/prestadores/10/publico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaPontualidade").value(4.8))
                .andExpect(jsonPath("$.mediaQualidade").doesNotExist())
                .andExpect(jsonPath("$.mediaComunicacao").doesNotExist());
    }

    // ── GET sem autenticação → 200 (endpoint público) ────────────────────────

    @Test
    @DisplayName("GET /api/prestadores/10/publico sem autenticação → 200 (endpoint público)")
    void perfilPublico_SemAutenticacao_Retorna200() throws Exception {
        when(prestadorService.buscarPerfilPublico(10L))
                .thenReturn(buildPerfilDTO(4.8, 4.5, 4.2));

        mockMvc.perform(get("/api/prestadores/10/publico"))
                .andExpect(status().isOk());
    }

    // ── GET prestador inexistente → 404 ──────────────────────────────────────

    @Test
    @DisplayName("GET /api/prestadores/99/publico prestador inexistente → 404")
    void perfilPublico_PrestadorInexistente_Retorna404() throws Exception {
        when(prestadorService.buscarPerfilPublico(99L))
                .thenThrow(new com.marketplace.servicos.exception.ResourceNotFoundException("Perfil não encontrado"));

        mockMvc.perform(get("/api/prestadores/99/publico"))
                .andExpect(status().isNotFound());
    }
}
