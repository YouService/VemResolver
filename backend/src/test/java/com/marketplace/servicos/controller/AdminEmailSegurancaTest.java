package com.marketplace.servicos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.ConfiguracaoEmailDTO;
import com.marketplace.servicos.dto.TesteEmailDTO;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.EmailConfigService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AdminController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class AdminEmailSegurancaTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserRepository userRepository;
    @MockBean PrestadorRepository prestadorRepository;
    @MockBean SolicitacaoServicoRepository solicitacaoServicoRepository;
    @MockBean PropostaRepository propostaRepository;
    @MockBean EmailConfigService emailConfigService;
    @MockBean JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // ── GET /api/admin/email/config ──────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/email/config — sem autenticação → 403")
    void getEmailConfig_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/email/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/email/config — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void getEmailConfig_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/email/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/email/config — como PRESTADOR → 403")
    @WithMockUser(roles = "PRESTADOR")
    void getEmailConfig_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/email/config"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/email/config — como ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void getEmailConfig_ComoAdmin_Retorna200() throws Exception {
        when(emailConfigService.buscar()).thenReturn(new ConfiguracaoEmailDTO());
        mockMvc.perform(get("/api/admin/email/config"))
                .andExpect(status().isOk());
    }

    // ── PUT /api/admin/email/config ──────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/admin/email/config — sem autenticação → 403")
    void salvarEmailConfig_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(put("/api/admin/email/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\":true}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/email/config — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void salvarEmailConfig_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(put("/api/admin/email/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ativo\":true}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/admin/email/config — como ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void salvarEmailConfig_ComoAdmin_Retorna200() throws Exception {
        ConfiguracaoEmailDTO dto = new ConfiguracaoEmailDTO();
        dto.setAtivo(true);
        when(emailConfigService.salvar(any())).thenReturn(dto);

        mockMvc.perform(put("/api/admin/email/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // ── POST /api/admin/email/testar ─────────────────────────────────────────

    @Test
    @DisplayName("POST /api/admin/email/testar — sem autenticação → 403")
    void testarEmail_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(post("/api/admin/email/testar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailDestino\":\"test@test.com\"}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/email/testar — como PRESTADOR → 403")
    @WithMockUser(roles = "PRESTADOR")
    void testarEmail_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(post("/api/admin/email/testar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailDestino\":\"test@test.com\"}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/admin/email/testar — como ADMIN → 204")
    @WithMockUser(roles = "ADMIN")
    void testarEmail_ComoAdmin_Retorna204() throws Exception {
        doNothing().when(emailConfigService).enviarTeste(any());

        TesteEmailDTO dto = new TesteEmailDTO();
        dto.setEmailDestino("admin@test.com");

        mockMvc.perform(post("/api/admin/email/testar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/admin/email/testar — email inválido → 400")
    @WithMockUser(roles = "ADMIN")
    void testarEmail_EmailInvalido_Retorna400() throws Exception {
        mockMvc.perform(post("/api/admin/email/testar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailDestino\":\"nao-e-um-email\"}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}
