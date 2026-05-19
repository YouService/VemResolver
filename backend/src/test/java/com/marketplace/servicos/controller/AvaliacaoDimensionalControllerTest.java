package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.AvaliacaoDTO;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.AvaliacaoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AvaliacaoController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class AvaliacaoDimensionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private AvaliacaoService avaliacaoService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User cliente;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        cliente = new User();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@cliente.com");
        cliente.setLogin("joao@cliente.com");
        cliente.setSenha("pass");
        cliente.setTipo(TipoUsuario.CLIENTE);
        cliente.setAtivo(true);
        cliente.setAvaliacaoMedia(0.0);
        cliente.setTotalAvaliacoes(0);
    }

    private AvaliacaoDTO buildRespostaDTO(Integer pontualidade, Integer qualidade, Integer comunicacao) {
        AvaliacaoDTO dto = new AvaliacaoDTO();
        dto.setId(1L);
        dto.setSolicitacaoId(5L);
        dto.setNota(5);
        dto.setNotaPontualidade(pontualidade);
        dto.setNotaQualidade(qualidade);
        dto.setNotaComunicacao(comunicacao);
        return dto;
    }

    // ── POST com todas as dimensões ──────────────────────────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes com 3 dimensões → 201 e campos dimensionais na resposta")
    void criar_ComTodasDimensoes_Retorna201ComCamposNoBody() throws Exception {
        when(avaliacaoService.criar(any(AvaliacaoDTO.class), eq(1L)))
                .thenReturn(buildRespostaDTO(5, 4, 3));

        mockMvc.perform(post("/api/avaliacoes")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solicitacaoId": 5,
                                  "prestadorId": 10,
                                  "nota": 5,
                                  "notaPontualidade": 5,
                                  "notaQualidade": 4,
                                  "notaComunicacao": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notaPontualidade").value(5))
                .andExpect(jsonPath("$.notaQualidade").value(4))
                .andExpect(jsonPath("$.notaComunicacao").value(3));
    }

    // ── POST sem dimensões → campos nulos omitidos ───────────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes sem dimensões → 201 e campos dimensionais ausentes na resposta")
    void criar_SemDimensoes_Retorna201ComCamposDimensionaisNulos() throws Exception {
        when(avaliacaoService.criar(any(AvaliacaoDTO.class), eq(1L)))
                .thenReturn(buildRespostaDTO(null, null, null));

        mockMvc.perform(post("/api/avaliacoes")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solicitacaoId": 5,
                                  "prestadorId": 10,
                                  "nota": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(5))
                .andExpect(jsonPath("$.notaPontualidade").doesNotExist())
                .andExpect(jsonPath("$.notaQualidade").doesNotExist())
                .andExpect(jsonPath("$.notaComunicacao").doesNotExist());
    }

    // ── POST com dimensão = 0 → 400 (abaixo do mínimo) ──────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes com notaPontualidade=0 → 400 violação @Min(1)")
    void criar_NotaPontualidadeZero_Retorna400() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solicitacaoId": 5,
                                  "prestadorId": 10,
                                  "nota": 4,
                                  "notaPontualidade": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── POST com dimensão = 6 → 400 (acima do máximo) ───────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes com notaQualidade=6 → 400 violação @Max(5)")
    void criar_NotaQualidadeAcimaDoMaximo_Retorna400() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solicitacaoId": 5,
                                  "prestadorId": 10,
                                  "nota": 4,
                                  "notaQualidade": 6
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── POST com nota geral ausente → 400 ───────────────────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes sem nota geral → 400 violação @NotNull")
    void criar_SemNotaGeral_Retorna400() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solicitacaoId": 5,
                                  "prestadorId": 10,
                                  "notaPontualidade": 4
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── POST sem autenticação → 403 ──────────────────────────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes sem autenticação → 403")
    void criar_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "solicitacaoId": 5,
                                  "prestadorId": 10,
                                  "nota": 4
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    // ── POST sem solicitacaoId → 400 (@NotNull) ──────────────────────────────

    @Test
    @DisplayName("POST /api/avaliacoes sem solicitacaoId → 400 violação @NotNull")
    void criar_SemSolicitacaoId_Retorna400() throws Exception {
        mockMvc.perform(post("/api/avaliacoes")
                        .with(user(cliente))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "prestadorId": 10,
                                  "nota": 4
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
