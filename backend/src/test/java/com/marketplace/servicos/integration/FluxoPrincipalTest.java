package com.marketplace.servicos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.servicos.dto.LoginDTO;
import com.marketplace.servicos.dto.RegisterDTO;
import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FluxoPrincipalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // Limpar usuários de teste se existirem para garantir que o teste rode limpo
        List<String> emails = List.of(
            "cliente001@teste.com", 
            "cliente002@teste.com", 
            "prestador001@teste.com", 
            "prestador002@teste.com"
        );
        
        for (String email : emails) {
            userRepository.findByEmail(email).ifPresent(user -> userRepository.delete(user));
        }
    }

    @Test
    @DisplayName("Deve cadastrar usuários e criar solicitação")
    void fluxoCompleto() throws Exception {
        System.out.println(">>> INICIANDO TESTE DE FLUXO PRINCIPAL <<<");

        // =================================================================================
        // 1. CADASTRO DOS USUÁRIOS
        // =================================================================================

        // Cliente 001
        cadastrarUsuario("Cliente 001", "cliente001@teste.com", "123Quatro@", TipoUsuario.CLIENTE);
        
        // Cliente 002
        cadastrarUsuario("Cliente 002", "cliente002@teste.com", "223Quatro@", TipoUsuario.CLIENTE);

        // Prestador 001
        cadastrarUsuario("Prestador 001", "prestador001@teste.com", "123Quatro@", TipoUsuario.PRESTADOR);

        // Prestador 002
        cadastrarUsuario("Prestador 002", "prestador002@teste.com", "223Quatro@", TipoUsuario.PRESTADOR);

        System.out.println(">>> Usuários cadastrados com sucesso.");

        // =================================================================================
        // 2. ATIVAÇÃO MANUAL (Simulando clique no email)
        // =================================================================================
        // Como o sistema envia email e não ativa direto, precisamos ativar via banco para o teste prosseguir
        ativarUsuario("cliente001@teste.com");
        ativarUsuario("cliente002@teste.com");
        ativarUsuario("prestador001@teste.com");
        ativarUsuario("prestador002@teste.com");
        
        System.out.println(">>> Usuários ativados manualmente.");

        // =================================================================================
        // 3. LOGIN (Cliente 001)
        // =================================================================================
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("cliente001@teste.com");
        loginDTO.setSenha("123Quatro@");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();
        
        System.out.println(">>> Login realizado. Token obtido.");

        // =================================================================================
        // 4. CRIAR SOLICITAÇÃO (Cliente 001)
        // =================================================================================
        SolicitacaoServicoDTO solicitacao = new SolicitacaoServicoDTO();
        // Usamos ID 1 assumindo que o DataSeeder já populou as categorias. 
        // Se não, o teste falhará aqui (Category not found).
        solicitacao.setCategoriaId(1L); 
        solicitacao.setDescricao("Preciso trocar a fiação do chuveiro urgente.");
        solicitacao.setCidade("São Paulo");
        solicitacao.setEstado("SP");

        mockMvc.perform(post("/api/solicitacoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitacao)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descricao").value("Preciso trocar a fiação do chuveiro urgente."));
        
        System.out.println(">>> Solicitação criada com sucesso!");
    }

    private void cadastrarUsuario(String nome, String email, String senha, TipoUsuario tipo) throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setNome(nome);
        dto.setEmail(email);
        dto.setSenha(senha);
        dto.setTipo(tipo);
        dto.setTelefone("11999999999");
        dto.setCpf(tipo == TipoUsuario.CLIENTE ? "111.111.111-11" : "222.222.222-22"); // Formato simples, backend pode ter máscara

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    private void ativarUsuario(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setAtivo(true);
        userRepository.save(user);
    }
}