package com.marketplace.servicos.config;

import com.marketplace.servicos.model.Categoria;
import com.marketplace.servicos.model.TipoUsuario;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.CategoriaRepository;
import com.marketplace.servicos.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!prod")
public class DataSeeder {
    
    @Bean
    CommandLineRunner initDatabase(
            CategoriaRepository categoriaRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (categoriaRepository.count() == 0) {
                List<Categoria> categorias = List.of(
                        new Categoria(null, "Eletricista", "Serviços elétricos residenciais e comerciais"),
                        new Categoria(null, "Encanador", "Reparos e instalações hidráulicas"),
                        new Categoria(null, "Pintor", "Pintura residencial e comercial"),
                        new Categoria(null, "Pedreiro", "Construção e reformas"),
                        new Categoria(null, "Marceneiro", "Móveis e serviços em madeira"),
                        new Categoria(null, "Jardineiro", "Manutenção e paisagismo"),
                        new Categoria(null, "Diarista", "Limpeza residencial e comercial"),
                        new Categoria(null, "Professor Particular", "Aulas particulares diversas matérias"),
                        new Categoria(null, "Mecânico", "Manutenção automotiva"),
                        new Categoria(null, "Desenvolvedor", "Desenvolvimento de software")
                );
                categoriaRepository.saveAll(categorias);
                System.out.println("✓ Categorias iniciais criadas");
            }
            
            if (!userRepository.existsByEmail("joao@cliente.com")) {
                User cliente = new User();
                cliente.setNome("João Silva");
                cliente.setLogin("joao");
                cliente.setEmail("joao@cliente.com");
                cliente.setSenha(passwordEncoder.encode("123456"));
                cliente.setTipo(TipoUsuario.CLIENTE);
                cliente.setTelefone("11999999999");
                cliente.setDataCadastro(LocalDateTime.now());
                cliente.setAtivo(true);
                userRepository.save(cliente);
                System.out.println("✓ Usuário de teste criado: joao@cliente.com / 123456");
            }

            if (!userRepository.existsByEmail("maria@prestador.com")) {
                User prestador = new User();
                prestador.setNome("Maria Santos");
                prestador.setLogin("maria");
                prestador.setEmail("maria@prestador.com");
                prestador.setSenha(passwordEncoder.encode("123456"));
                prestador.setTipo(TipoUsuario.PRESTADOR);
                prestador.setTelefone("11988888888");
                prestador.setDataCadastro(LocalDateTime.now());
                prestador.setAtivo(true);
                userRepository.save(prestador);
                System.out.println("✓ Usuário de teste criado: maria@prestador.com / 123456");
            }

            if (!userRepository.existsByEmail("admin@vemresolver.com")) {
                User admin = new User();
                admin.setNome("Administrador");
                admin.setLogin("admin");
                admin.setEmail("admin@vemresolver.com");
                admin.setSenha(passwordEncoder.encode("Admin@2025"));
                admin.setTipo(TipoUsuario.ADMIN);
                admin.setTelefone("11900000000");
                admin.setDataCadastro(LocalDateTime.now());
                admin.setAtivo(true);
                userRepository.save(admin);
                System.out.println("✓ Usuário de teste criado: admin@vemresolver.com / Admin@2025");
            }
        };
    }
}
