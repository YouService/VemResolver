package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.repository.AdminAuditLogRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.PropostaRepository;
import com.marketplace.servicos.repository.SolicitacaoServicoRepository;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.AdminAuditService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import org.springframework.data.domain.PageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = AdminController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class AdminSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserRepository userRepository;
    @MockBean private PrestadorRepository prestadorRepository;
    @MockBean private SolicitacaoServicoRepository solicitacaoServicoRepository;
    @MockBean private PropostaRepository propostaRepository;
    @MockBean private EmailConfigService emailConfigService;
    @MockBean private AdminAuditService adminAuditService;
    @MockBean private AdminAuditLogRepository adminAuditLogRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // ── GET /api/admin/stats ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/stats — sem autenticação → 403")
    void stats_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/stats — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void stats_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/stats — como PRESTADOR → 403")
    @WithMockUser(roles = "PRESTADOR")
    void stats_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/stats — como ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void stats_ComoAdmin_Retorna200() throws Exception {
        when(userRepository.count()).thenReturn(10L);
        when(prestadorRepository.count()).thenReturn(5L);
        when(solicitacaoServicoRepository.count()).thenReturn(20L);
        when(propostaRepository.count()).thenReturn(15L);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk());
    }

    // ── GET /api/admin/usuarios ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/usuarios — sem autenticação → 403")
    void listarUsuarios_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/usuarios — como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void listarUsuarios_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/usuarios — como ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void listarUsuarios_ComoAdmin_Retorna200() throws Exception {
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/admin/usuarios/{id}/ativar ────────────────────────────────

    @Test
    @DisplayName("PATCH /api/admin/usuarios/1/ativar — sem autenticação → 403")
    void ativarUsuario_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/usuarios/1/ativar")
                        .param("ativo", "false")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/admin/usuarios/1/ativar — como PRESTADOR → 403")
    @WithMockUser(roles = "PRESTADOR")
    void ativarUsuario_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/usuarios/1/ativar")
                        .param("ativo", "false")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/usuarios/online ─────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/usuarios/online — sem autenticacao -> 403")
    void listarOnline_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios/online"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/usuarios/online — como CLIENTE -> 403")
    @WithMockUser(roles = "CLIENTE")
    void listarOnline_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios/online"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/usuarios/online — como ADMIN -> 200")
    @WithMockUser(roles = "ADMIN")
    void listarOnline_ComoAdmin_Retorna200() throws Exception {
        when(userRepository.findOnline(any(java.time.LocalDateTime.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/admin/usuarios/online"))
                .andExpect(status().isOk());
    }

    // ── GET /api/admin/solicitacoes ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/admin/solicitacoes — sem autenticacao -> 403")
    void listarSolicitacoes_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/solicitacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/solicitacoes — como PRESTADOR -> 403")
    @WithMockUser(roles = "PRESTADOR")
    void listarSolicitacoes_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/solicitacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/admin/solicitacoes — como ADMIN -> 200")
    @WithMockUser(roles = "ADMIN")
    void listarSolicitacoes_ComoAdmin_Retorna200() throws Exception {
        when(solicitacaoServicoRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/admin/solicitacoes"))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/admin/solicitacoes/{id}/status ─────────────────────────────

    @Test
    @DisplayName("PATCH /api/admin/solicitacoes/1/status — sem autenticacao -> 403")
    void alterarStatusSolicitacao_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/solicitacoes/1/status")
                        .param("status", "CONCLUIDA")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/admin/solicitacoes/1/status — como CLIENTE -> 403")
    @WithMockUser(roles = "CLIENTE")
    void alterarStatusSolicitacao_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/solicitacoes/1/status")
                        .param("status", "CONCLUIDA")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/admin/solicitacoes/1/status — como ADMIN -> 204")
    @WithMockUser(roles = "ADMIN")
    void alterarStatusSolicitacao_ComoAdmin_Retorna204() throws Exception {
        com.marketplace.servicos.model.SolicitacaoServico sol = new com.marketplace.servicos.model.SolicitacaoServico();
        sol.setId(1L);
        sol.setStatus(com.marketplace.servicos.model.StatusSolicitacao.ABERTA);
        when(solicitacaoServicoRepository.findById(1L)).thenReturn(java.util.Optional.of(sol));
        when(solicitacaoServicoRepository.save(any())).thenReturn(sol);

        mockMvc.perform(patch("/api/admin/solicitacoes/1/status")
                        .param("status", "CONCLUIDA")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
