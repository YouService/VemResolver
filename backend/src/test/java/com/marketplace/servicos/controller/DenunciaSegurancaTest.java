package com.marketplace.servicos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.AtualizarDenunciaDTO;
import com.marketplace.servicos.dto.CriarDenunciaDTO;
import com.marketplace.servicos.dto.DenunciaDTO;
import com.marketplace.servicos.model.StatusDenuncia;
import com.marketplace.servicos.model.TipoDenuncia;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.DenunciaService;
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
    value = DenunciaController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class DenunciaSegurancaTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DenunciaService denunciaService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User clienteUser;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        clienteUser = new User();
        clienteUser.setId(1L);
        clienteUser.setNome("João Cliente");
        clienteUser.setEmail("joao@test.com");
        clienteUser.setLogin("joao@test.com");
        clienteUser.setSenha("pass");
        clienteUser.setTipo(TipoUsuario.CLIENTE);
        clienteUser.setAtivo(true);
        clienteUser.setAvaliacaoMedia(0.0);
        clienteUser.setTotalAvaliacoes(0);

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

    // ── DS-01: POST /api/denuncias sem autenticação → 403 ────────────────────

    @Test
    @DisplayName("DS-01: POST /api/denuncias sem autenticação → 403")
    void criarDenuncia_SemAuth_Retorna403() throws Exception {
        CriarDenunciaDTO dto = buildCriarDTO();
        mockMvc.perform(post("/api/denuncias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── DS-02: POST /api/denuncias como CLIENTE autenticado → 201 ────────────

    @Test
    @DisplayName("DS-02: POST /api/denuncias como CLIENTE autenticado → 201")
    void criarDenuncia_ComoCliente_Retorna201() throws Exception {
        DenunciaDTO resposta = buildDenunciaDTO();
        when(denunciaService.criar(any(), any())).thenReturn(resposta);

        CriarDenunciaDTO dto = buildCriarDTO();
        mockMvc.perform(post("/api/denuncias")
                        .with(user(clienteUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    // ── DS-03: POST /api/denuncias — payload inválido (descricao curta) → 400 ─

    @Test
    @DisplayName("DS-03: POST /api/denuncias com descrição muito curta → 400")
    void criarDenuncia_DescricaoCurta_Retorna400() throws Exception {
        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(2L);
        dto.setTipo(TipoDenuncia.PERFIL);
        dto.setDescricao("curto");

        mockMvc.perform(post("/api/denuncias")
                        .with(user(clienteUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── DS-04: GET /api/admin/denuncias sem autenticação → 403 ───────────────

    @Test
    @DisplayName("DS-04: GET /api/admin/denuncias sem autenticação → 403")
    void listarDenuncias_SemAuth_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/denuncias"))
                .andExpect(status().isForbidden());
    }

    // ── DS-05: GET /api/admin/denuncias como CLIENTE → 403 ───────────────────

    @Test
    @DisplayName("DS-05: GET /api/admin/denuncias como CLIENTE → 403")
    void listarDenuncias_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/denuncias").with(user(clienteUser)))
                .andExpect(status().isForbidden());
    }

    // ── DS-06: GET /api/admin/denuncias como ADMIN → 200 ─────────────────────

    @Test
    @DisplayName("DS-06: GET /api/admin/denuncias como ADMIN → 200")
    void listarDenuncias_ComoAdmin_Retorna200() throws Exception {
        when(denunciaService.listar(isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/admin/denuncias").with(user(adminUser)))
                .andExpect(status().isOk());
    }

    // ── DS-07: PATCH /api/admin/denuncias/{id}/status sem autenticação → 403 ──

    @Test
    @DisplayName("DS-07: PATCH /api/admin/denuncias/1/status sem autenticação → 403")
    void atualizarStatus_SemAuth_Retorna403() throws Exception {
        AtualizarDenunciaDTO dto = new AtualizarDenunciaDTO();
        dto.setStatus(StatusDenuncia.APROVADA);

        mockMvc.perform(patch("/api/admin/denuncias/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ── DS-08: PATCH /api/admin/denuncias/{id}/status como ADMIN → 200 ────────

    @Test
    @DisplayName("DS-08: PATCH /api/admin/denuncias/1/status como ADMIN → 200")
    void atualizarStatus_ComoAdmin_Retorna200() throws Exception {
        DenunciaDTO resposta = buildDenunciaDTO();
        resposta.setStatus(StatusDenuncia.APROVADA);
        when(denunciaService.atualizarStatus(any(), any())).thenReturn(resposta);

        AtualizarDenunciaDTO dto = new AtualizarDenunciaDTO();
        dto.setStatus(StatusDenuncia.APROVADA);

        mockMvc.perform(patch("/api/admin/denuncias/1/status")
                        .with(user(adminUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CriarDenunciaDTO buildCriarDTO() {
        CriarDenunciaDTO dto = new CriarDenunciaDTO();
        dto.setDenunciadoId(2L);
        dto.setTipo(TipoDenuncia.PERFIL);
        dto.setDescricao("Perfil falso com informações completamente enganosas sobre os serviços");
        return dto;
    }

    private DenunciaDTO buildDenunciaDTO() {
        DenunciaDTO dto = new DenunciaDTO();
        dto.setId(10L);
        dto.setDenuncianteId(1L);
        dto.setDenuncianteNome("João Cliente");
        dto.setDenunciadoId(2L);
        dto.setDenunciadoNome("Maria Prestador");
        dto.setTipo(TipoDenuncia.PERFIL);
        dto.setDescricao("Perfil falso com informações completamente enganosas sobre os serviços");
        dto.setStatus(StatusDenuncia.PENDENTE);
        return dto;
    }
}
