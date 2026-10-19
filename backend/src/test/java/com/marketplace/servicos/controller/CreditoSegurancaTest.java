package com.marketplace.servicos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.ComprarCreditosDTO;
import com.marketplace.servicos.dto.CompraCreditoDTO;
import com.marketplace.servicos.dto.PacoteCreditoDTO;
import com.marketplace.servicos.dto.ResumoCreditosDTO;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.AdminAuditService;
import com.marketplace.servicos.service.CreditoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = CreditoController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class CreditoSegurancaTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CreditoService creditoService;
    @MockBean private AdminAuditService adminAuditService;
    @MockBean private UserRepository userRepository;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User prestadorUser;
    private User clienteUser;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        prestadorUser = buildUser(2L, "prestador@test.com", TipoUsuario.PRESTADOR);
        clienteUser = buildUser(1L, "cliente@test.com", TipoUsuario.CLIENTE);
        adminUser = buildUser(99L, "admin@vemresolver.com", TipoUsuario.ADMIN);
    }

    private User buildUser(Long id, String email, TipoUsuario tipo) {
        User u = new User();
        u.setId(id);
        u.setNome(email);
        u.setEmail(email);
        u.setLogin(email);
        u.setSenha("pass");
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setAvaliacaoMedia(0.0);
        u.setTotalAvaliacoes(0);
        return u;
    }

    private PacoteCreditoDTO buildPacote() {
        PacoteCreditoDTO dto = new PacoteCreditoDTO();
        dto.setId(1L);
        dto.setNome("Inicial");
        dto.setQuantidadeCreditos(5);
        dto.setPrecoCentavos(1990L);
        dto.setAtivo(true);
        return dto;
    }

    // ── Prestador ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CS-01: GET /api/prestadores/me/creditos sem autenticação → 403")
    void resumo_SemAuth_Retorna403() throws Exception {
        mockMvc.perform(get("/api/prestadores/me/creditos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CS-02: GET /api/prestadores/me/creditos como CLIENTE → 403")
    void resumo_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(get("/api/prestadores/me/creditos").with(user(clienteUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CS-03: GET /api/prestadores/me/creditos como PRESTADOR → 200")
    void resumo_ComoPrestador_Retorna200() throws Exception {
        when(creditoService.resumo(eq(2L), any())).thenReturn(new ResumoCreditosDTO());
        mockMvc.perform(get("/api/prestadores/me/creditos").with(user(prestadorUser)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CS-04: POST /api/prestadores/me/creditos/comprar como PRESTADOR → 201")
    void comprar_ComoPrestador_Retorna201() throws Exception {
        when(creditoService.comprar(2L, 1L)).thenReturn(new CompraCreditoDTO());
        ComprarCreditosDTO dto = new ComprarCreditosDTO();
        dto.setPacoteId(1L);

        mockMvc.perform(post("/api/prestadores/me/creditos/comprar")
                        .with(user(prestadorUser)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CS-05: POST /api/prestadores/me/creditos/comprar sem pacote → 400")
    void comprar_SemPacote_Retorna400() throws Exception {
        mockMvc.perform(post("/api/prestadores/me/creditos/comprar")
                        .with(user(prestadorUser)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("CS-06: POST /api/admin/pacotes como PRESTADOR → 403")
    void criarPacote_ComoPrestador_Retorna403() throws Exception {
        mockMvc.perform(post("/api/admin/pacotes")
                        .with(user(prestadorUser)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPacote())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CS-07: POST /api/admin/pacotes como ADMIN → 201")
    void criarPacote_ComoAdmin_Retorna201() throws Exception {
        when(creditoService.salvarPacote(isNull(), any())).thenReturn(buildPacote());
        mockMvc.perform(post("/api/admin/pacotes")
                        .with(user(adminUser)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildPacote())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CS-08: GET /api/admin/pacotes como ADMIN → 200")
    void listarPacotes_ComoAdmin_Retorna200() throws Exception {
        when(creditoService.listarTodosPacotes()).thenReturn(List.of(buildPacote()));
        mockMvc.perform(get("/api/admin/pacotes").with(user(adminUser)))
                .andExpect(status().isOk());
    }
}
