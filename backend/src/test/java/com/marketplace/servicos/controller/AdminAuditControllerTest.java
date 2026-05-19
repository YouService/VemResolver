package com.marketplace.servicos.controller;

import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.AdminAuditLogDTO;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.AdminAuditLogRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AdminController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private AdminAuditLogRepository adminAuditLogRepository;
    @MockBean private AdminAuditService adminAuditService;
    @MockBean private EmailConfigService emailConfigService;
    @MockBean private com.marketplace.servicos.repository.PrestadorRepository prestadorRepository;
    @MockBean private com.marketplace.servicos.repository.PropostaRepository propostaRepository;
    @MockBean private com.marketplace.servicos.repository.SolicitacaoServicoRepository solicitacaoServicoRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User admin;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        admin = new User();
        admin.setId(99L);
        admin.setNome("Admin");
        admin.setEmail("admin@vemresolver.com");
        admin.setLogin("admin@vemresolver.com");
        admin.setSenha("pass");
        admin.setTipo(TipoUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAvaliacaoMedia(0.0);
        admin.setTotalAvaliacoes(0);
    }

    // ── GET /api/admin/auditoria → 200 com página de registros ───────────────

    @Test
    @DisplayName("GET /api/admin/auditoria como ADMIN → 200 com registros")
    void auditoria_ComoAdmin_Retorna200ComRegistros() throws Exception {
        AdminAuditLogDTO entry = new AdminAuditLogDTO();
        entry.setId(1L);
        entry.setAdminId(99L);
        entry.setAdminNome("Admin");
        entry.setAcao("ATIVAR_USUARIO");
        entry.setRecursoTipo("USUARIO");
        entry.setRecursoId(5L);
        entry.setStatus("SUCESSO");
        entry.setCriadoEm(LocalDateTime.now());

        when(adminAuditLogRepository.filtrar(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/admin/auditoria").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── GET /api/admin/auditoria com filtro de acao ───────────────────────────

    @Test
    @DisplayName("GET /api/admin/auditoria?acao=ATIVAR_USUARIO → 200")
    void auditoria_ComFiltroAcao_Retorna200() throws Exception {
        when(adminAuditLogRepository.filtrar(eq("ATIVAR_USUARIO"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/admin/auditoria?acao=ATIVAR_USUARIO").with(user(admin)))
                .andExpect(status().isOk());
    }

    // ── GET /api/admin/auditoria sem autenticação → 403 ─────────────────────

    @Test
    @DisplayName("GET /api/admin/auditoria sem autenticação → 403")
    void auditoria_SemAutenticacao_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/auditoria"))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/auditoria como CLIENTE → 403 ──────────────────────────

    @Test
    @DisplayName("GET /api/admin/auditoria como CLIENTE → 403")
    void auditoria_ComoCliente_Retorna403() throws Exception {
        User cliente = new User();
        cliente.setId(2L);
        cliente.setNome("Cliente");
        cliente.setEmail("c@c.com");
        cliente.setLogin("c@c.com");
        cliente.setSenha("pass");
        cliente.setTipo(TipoUsuario.CLIENTE);
        cliente.setAtivo(true);
        cliente.setAvaliacaoMedia(0.0);
        cliente.setTotalAvaliacoes(0);

        mockMvc.perform(get("/api/admin/auditoria").with(user(cliente)))
                .andExpect(status().isForbidden());
    }
}
