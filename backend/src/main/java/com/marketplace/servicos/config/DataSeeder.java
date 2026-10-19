package com.marketplace.servicos.config;

import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!prod")
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            CategoriaRepository categoriaRepository,
            UserRepository userRepository,
            PrestadorRepository prestadorRepository,
            SolicitacaoServicoRepository solicitacaoRepository,
            PropostaRepository propostaRepository,
            PacoteCreditoRepository pacoteCreditoRepository,
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

            seedDadosChat(categoriaRepository, userRepository, prestadorRepository, solicitacaoRepository, propostaRepository);
            seedPacotesCreditoFicticios(pacoteCreditoRepository);
        };
    }

    // Pacotes de exemplo para desenvolvimento. Em produção os pacotes reais
    // são cadastrados pelo admin em /api/admin/pacotes.
    private void seedPacotesCreditoFicticios(PacoteCreditoRepository pacoteCreditoRepository) {
        if (pacoteCreditoRepository.count() > 0) {
            return;
        }
        pacoteCreditoRepository.saveAll(List.of(
                pacoteFicticio("Inicial", 5, 1990L),
                pacoteFicticio("Profissional", 15, 4990L),
                pacoteFicticio("Premium", 40, 11990L)
        ));
        System.out.println("✓ Pacotes de crédito fictícios criados");
    }

    private PacoteCredito pacoteFicticio(String nome, int creditos, long precoCentavos) {
        PacoteCredito pacote = new PacoteCredito();
        pacote.setNome(nome);
        pacote.setDescricao("[FICTÍCIO] " + creditos + " créditos — valor de exemplo para desenvolvimento");
        pacote.setQuantidadeCreditos(creditos);
        pacote.setPrecoCentavos(precoCentavos);
        pacote.setAtivo(true);
        return pacote;
    }

    private void seedDadosChat(
            CategoriaRepository categoriaRepository,
            UserRepository userRepository,
            PrestadorRepository prestadorRepository,
            SolicitacaoServicoRepository solicitacaoRepository,
            PropostaRepository propostaRepository) {

        User joao = userRepository.findByEmail("joao@cliente.com").orElse(null);
        User maria = userRepository.findByEmail("maria@prestador.com").orElse(null);
        if (joao == null || maria == null) return;

        Categoria eletricista = categoriaRepository.findAll().stream()
                .filter(c -> c.getNome().equals("Eletricista"))
                .findFirst().orElse(null);
        if (eletricista == null) return;

        Prestador prestadorMaria = prestadorRepository.findByUsuarioId(maria.getId()).orElseGet(() -> {
            Prestador p = new Prestador();
            p.setUsuario(maria);
            p.setDescricaoPerfil("Eletricista com 10 anos de experiência em instalações residenciais e comerciais.");
            p.setCidade("São Paulo");
            p.setEstado("SP");
            p.setAvaliacaoMedia(0.0);
            p.setTotalAvaliacoes(0);
            p.getCategorias().add(eletricista);
            Prestador salvo = prestadorRepository.save(p);
            System.out.println("✓ Perfil de prestador criado: maria@prestador.com");
            return salvo;
        });

        // Cria dados de teste apenas se Maria não tiver nenhuma proposta ENVIADA
        long propostasEnviadas = propostaRepository
                .findByPrestadorIdAndStatus(prestadorMaria.getId(), StatusProposta.ENVIADA, PageRequest.of(0, 1))
                .getTotalElements();

        if (propostasEnviadas == 0) {
            SolicitacaoServico solicitacao = new SolicitacaoServico();
            solicitacao.setCliente(joao);
            solicitacao.setCategoria(eletricista);
            solicitacao.setDescricao("Instalação de tomada elétrica na sala de estar e troca de disjuntor na caixa de fusíveis.");
            solicitacao.setCidade("São Paulo");
            solicitacao.setEstado("SP");
            solicitacao = solicitacaoRepository.save(solicitacao);
            System.out.println("✓ Solicitação de teste criada (ABERTA)");

            Proposta proposta = new Proposta();
            proposta.setSolicitacao(solicitacao);
            proposta.setPrestador(prestadorMaria);
            proposta.setValor(new BigDecimal("150.00"));
            proposta.setMensagem("Posso realizar o serviço ainda esta semana. Tenho disponibilidade para segunda e terça-feira pela manhã.");
            propostaRepository.save(proposta);
            System.out.println("✓ Proposta de teste criada (ENVIADA) — acesse o chat em /prestador/dashboard ou Propostas Recebidas");
        }
    }
}
