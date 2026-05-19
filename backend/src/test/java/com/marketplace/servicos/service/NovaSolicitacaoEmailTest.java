package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.SolicitacaoServicoDTO;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NovaSolicitacaoEmailTest {

    @Mock private SolicitacaoServicoRepository solicitacaoRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private PropostaRepository propostaRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private NotificacaoService notificacaoService;
    @Mock private EmailService emailService;
    @Mock private PrestadorService prestadorService;
    @Mock private WebPushService webPushService;

    @InjectMocks
    private SolicitacaoServicoService solicitacaoService;

    private User cliente;
    private Categoria categoria;
    private SolicitacaoServicoDTO dto;

    @BeforeEach
    void setUp() {
        cliente = new User();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@cliente.com");
        cliente.setTipo(TipoUsuario.CLIENTE);

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNome("Elétrica");

        dto = new SolicitacaoServicoDTO();
        dto.setCategoriaId(1L);
        dto.setDescricao("Troca de fiação elétrica");
        dto.setCidade("São Paulo");
        dto.setEstado("SP");

        // Inject webPushService via reflection since it uses @Autowired @Lazy
        org.springframework.test.util.ReflectionTestUtils.setField(solicitacaoService, "webPushService", webPushService);
    }

    private Prestador buildPrestador(Long id, String email) {
        User u = new User();
        u.setId(id + 100L);
        u.setEmail(email);
        u.setNome("Prestador" + id);

        Prestador p = new Prestador();
        p.setId(id);
        p.setUsuario(u);
        p.setAvaliacaoMedia(0.0);
        p.setTotalAvaliacoes(0);
        return p;
    }

    // NS-01: criar solicitação com 2 prestadores na categoria → emailService.sendNovaSolicitacao chamado 2 vezes
    @Test
    @DisplayName("NS-01: criar solicitação com 2 prestadores da categoria → sendNovaSolicitacao chamado 2 vezes")
    void criar_ComDoisPrestadoresNaCategoria_EnviaEmailParaCada() {
        Prestador p1 = buildPrestador(1L, "p1@test.com");
        Prestador p2 = buildPrestador(2L, "p2@test.com");

        when(userRepository.findByIdAndTipo(1L, TipoUsuario.CLIENTE)).thenReturn(Optional.of(cliente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> {
            SolicitacaoServico s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(prestadorRepository.findByCategoria(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p1, p2)));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(anyLong(), any())).thenReturn(false);
        when(propostaRepository.findBySolicitacaoIdAndStatus(anyLong(), any())).thenReturn(Optional.empty());

        solicitacaoService.criar(dto, 1L);

        verify(emailService, times(2)).sendNovaSolicitacao(
                anyString(), eq("Elétrica"), eq("São Paulo"), eq("SP"), anyString());
    }

    // NS-02: criar solicitação sem prestadores na categoria → email não chamado
    @Test
    @DisplayName("NS-02: criar solicitação sem prestadores na categoria → sendNovaSolicitacao não chamado")
    void criar_SemPrestadoresNaCategoria_NaoEnviaEmail() {
        when(userRepository.findByIdAndTipo(1L, TipoUsuario.CLIENTE)).thenReturn(Optional.of(cliente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> {
            SolicitacaoServico s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(prestadorRepository.findByCategoria(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(anyLong(), any())).thenReturn(false);
        when(propostaRepository.findBySolicitacaoIdAndStatus(anyLong(), any())).thenReturn(Optional.empty());

        solicitacaoService.criar(dto, 1L);

        verify(emailService, never()).sendNovaSolicitacao(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    // NS-03: criar solicitação com 1 prestador → emailService.sendNovaSolicitacao chamado 1 vez
    @Test
    @DisplayName("NS-03: criar solicitação com 1 prestador na categoria → sendNovaSolicitacao chamado 1 vez")
    void criar_ComUmPrestadorNaCategoria_EnviaEmailUmaVez() {
        Prestador p1 = buildPrestador(1L, "p1@test.com");

        when(userRepository.findByIdAndTipo(1L, TipoUsuario.CLIENTE)).thenReturn(Optional.of(cliente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(solicitacaoRepository.save(any())).thenAnswer(inv -> {
            SolicitacaoServico s = inv.getArgument(0);
            s.setId(10L);
            return s;
        });
        when(prestadorRepository.findByCategoria(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(p1)));
        when(avaliacaoRepository.existsBySolicitacaoIdAndTipo(anyLong(), any())).thenReturn(false);
        when(propostaRepository.findBySolicitacaoIdAndStatus(anyLong(), any())).thenReturn(Optional.empty());

        solicitacaoService.criar(dto, 1L);

        verify(emailService, times(1)).sendNovaSolicitacao(
                eq("p1@test.com"), eq("Elétrica"), eq("São Paulo"), eq("SP"), anyString());
    }
}
