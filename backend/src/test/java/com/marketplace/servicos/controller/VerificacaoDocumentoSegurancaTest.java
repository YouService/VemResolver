package com.marketplace.servicos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.VerificacaoDocumentoService;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = VerificacaoDocumentoController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class VerificacaoDocumentoSegurancaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private VerificacaoDocumentoService verificacaoService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User prestadorUser;
    private User adminUser;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        prestadorUser = new User();
        prestadorUser.setId(10L);
        prestadorUser.setNome("Maria Prestador");
        prestadorUser.setEmail("maria@test.com");
        prestadorUser.setLogin("maria@test.com");
        prestadorUser.setSenha("pass");
        prestadorUser.setTipo(TipoUsuario.PRESTADOR);
        prestadorUser.setAtivo(true);
        prestadorUser.setAvaliacaoMedia(0.0);
        prestadorUser.setTotalAvaliacoes(0);

        adminUser = new User();
        adminUser.setId(99L);
        adminUser.setNome("Admin");
        adminUser.setEmail("admin@vemresolver.com");
        adminUser.setLogin("admin@vemresolver.com");
        adminUser.setSenha("pass");
        adminUser.setTipo(TipoUsuario.ADMIN);
        adminUser.setAtivo(true);
        adminUser.setAvaliacaoMedia(0.0);
        adminUser.setTotalAvaliacoes(0);
    }

    // ── GET /api/prestadores/me/verificacao ────────────────────────────────────

    @Test
    @DisplayName("VS-01: GET me/verificacao sem autenticação → 403")
    void consultarStatus_SemAuth_Retorna403() throws Exception {
        mockMvc.perform(get("/api/prestadores/me/verificacao"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VS-02: GET me/verificacao como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void consultarStatus_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/prestadores/me/verificacao"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VS-03: GET me/verificacao como PRESTADOR → 204 (sem verificação existente)")
    void consultarStatus_ComoPrestador_Retorna204() throws Exception {
        when(verificacaoService.buscarPorPrestador(10L)).thenReturn(null);
        mockMvc.perform(get("/api/prestadores/me/verificacao").with(user(prestadorUser)))
                .andExpect(status().isNoContent());
    }

    // ── POST /api/prestadores/me/verificacao ──────────────────────────────────

    @Test
    @DisplayName("VS-04: POST me/verificacao sem autenticação → 403")
    void enviarDocumentos_SemAuth_Retorna403() throws Exception {
        MockMultipartFile doc = new MockMultipartFile("documento", "cpf.jpg", "image/jpeg", "bytes".getBytes());
        MockMultipartFile selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "bytes".getBytes());
        mockMvc.perform(multipart("/api/prestadores/me/verificacao").file(doc).file(selfie))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VS-05: POST me/verificacao como ADMIN → 403 (apenas PRESTADOR)")
    @WithMockUser(roles = "ADMIN")
    void enviarDocumentos_ComoAdmin_Retorna403() throws Exception {
        MockMultipartFile doc = new MockMultipartFile("documento", "cpf.jpg", "image/jpeg", "bytes".getBytes());
        MockMultipartFile selfie = new MockMultipartFile("selfie", "selfie.jpg", "image/jpeg", "bytes".getBytes());
        mockMvc.perform(multipart("/api/prestadores/me/verificacao").file(doc).file(selfie).with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/verificacoes ──────────────────────────────────────────

    @Test
    @DisplayName("VS-06: GET /api/admin/verificacoes sem autenticação → 403")
    void listarVerificacoes_SemAuth_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/verificacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VS-07: GET /api/admin/verificacoes como PRESTADOR → 403")
    @WithMockUser(roles = "PRESTADOR")
    void listarVerificacoes_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/verificacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VS-08: GET /api/admin/verificacoes como ADMIN → 200")
    void listarVerificacoes_ComoAdmin_Retorna200() throws Exception {
        when(verificacaoService.listar(isNull(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        mockMvc.perform(get("/api/admin/verificacoes").with(user(adminUser)))
                .andExpect(status().isOk());
    }

    // ── PATCH /api/admin/verificacoes/{id}/aprovar ──────────────────────────

    @Test
    @DisplayName("VS-09: PATCH aprovar sem autenticação → 403")
    void aprovar_SemAuth_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/verificacoes/1/aprovar")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VS-10: PATCH aprovar como CLIENTE → 403")
    @WithMockUser(roles = "CLIENTE")
    void aprovar_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/verificacoes/1/aprovar")
                .contentType(MediaType.APPLICATION_JSON).content("{}").with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── PATCH /api/admin/verificacoes/{id}/recusar ───────────────────────────

    @Test
    @DisplayName("VS-11: PATCH recusar sem autenticação → 403")
    void recusar_SemAuth_Retorna403() throws Exception {
        mockMvc.perform(patch("/api/admin/verificacoes/1/recusar")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // ── GET /api/admin/verificacoes/{id}/documento ──────────────────────────

    @Test
    @DisplayName("VS-12: GET /api/admin/verificacoes/1/documento como PRESTADOR → 403")
    @WithMockUser(roles = "PRESTADOR")
    void verDocumento_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/verificacoes/1/documento"))
                .andExpect(status().isForbidden());
    }
}
