package com.marketplace.servicos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.servicos.config.SecurityConfig;
import com.marketplace.servicos.dto.CategoriaDTO;
import com.marketplace.servicos.repository.UserRepository;
import com.marketplace.servicos.security.CustomUserDetailsService;
import com.marketplace.servicos.security.JwtAuthenticationFilter;
import com.marketplace.servicos.service.CategoriaService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CategoriaController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.frontend.url=http://localhost:4200")
class CategoriaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CategoriaService categoriaService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(invocation -> {
            invocation.<FilterChain>getArgument(2)
                    .doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void getCategoria_SemAutenticacao_Retorna200() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    void postCategoria_SemAutenticacao_Retorna403() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "Nova", "Descrição");

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void postCategoria_ComoCliente_Retorna403() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "Nova", "Descrição");

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PRESTADOR")
    void postCategoria_ComoPrestador_Retorna403() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(null, "Nova", "Descrição");

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void putCategoria_ComoCliente_Retorna403() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(1L, "Editada", "Descrição");

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void deleteCategoria_ComoCliente_Retorna403() throws Exception {
        mockMvc.perform(delete("/api/categorias/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
