package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.CompraCreditoDTO;
import com.marketplace.servicos.dto.ResumoCreditosDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.ContaCreditoRepository;
import com.marketplace.servicos.repository.PacoteCreditoRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.TransacaoCreditoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditoServiceTest {

    @Mock private ContaCreditoRepository contaRepository;
    @Mock private TransacaoCreditoRepository transacaoRepository;
    @Mock private PacoteCreditoRepository pacoteRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private GatewayPagamento gatewayPagamento;

    @InjectMocks
    private CreditoService creditoService;

    private Prestador prestador;
    private Proposta proposta;
    private PacoteCredito pacote;

    @BeforeEach
    void setUp() {
        User usuario = new User();
        usuario.setId(2L);
        usuario.setEmail("prestador@test.com");

        prestador = new Prestador();
        prestador.setId(10L);
        prestador.setUsuario(usuario);

        proposta = new Proposta();
        proposta.setId(100L);
        proposta.setPrestador(prestador);

        pacote = new PacoteCredito();
        pacote.setId(1L);
        pacote.setNome("Inicial");
        pacote.setQuantidadeCreditos(5);
        pacote.setPrecoCentavos(1990L);
        pacote.setAtivo(true);
    }

    private ContaCredito conta(int saldo, int gratuitosUsados) {
        ContaCredito conta = new ContaCredito();
        conta.setId(50L);
        conta.setPrestador(prestador);
        conta.setSaldo(saldo);
        conta.setGratuitosUsados(gratuitosUsados);
        return conta;
    }

    private TransacaoCredito transacaoSalva() {
        ArgumentCaptor<TransacaoCredito> captor = ArgumentCaptor.forClass(TransacaoCredito.class);
        verify(transacaoRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    // ── CR-01..03: consumo no aceite ─────────────────────────────────────────

    @Test
    @DisplayName("CR-01: primeiro aceite cria a conta e usa o tier gratuito sem debitar saldo")
    void consumirPorAceite_ContaNova_UsaGratuito() {
        when(contaRepository.findByPrestadorIdParaAtualizacao(10L)).thenReturn(Optional.empty());
        when(contaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        creditoService.consumirPorAceite(proposta);

        TransacaoCredito t = transacaoSalva();
        assertThat(t.getTipo()).isEqualTo(TipoTransacaoCredito.CONSUMO_GRATUITO);
        assertThat(t.getQuantidade()).isZero();
        assertThat(t.getConta().getGratuitosUsados()).isEqualTo(1);
        assertThat(t.getConta().getSaldo()).isZero();
        assertThat(t.getProposta()).isSameAs(proposta);
    }

    @Test
    @DisplayName("CR-02: tier gratuito esgotado — debita 1 crédito do saldo")
    void consumirPorAceite_GratuitoEsgotado_DebitaSaldo() {
        ContaCredito conta = conta(5, 3);
        when(contaRepository.findByPrestadorIdParaAtualizacao(10L)).thenReturn(Optional.of(conta));

        creditoService.consumirPorAceite(proposta);

        TransacaoCredito t = transacaoSalva();
        assertThat(t.getTipo()).isEqualTo(TipoTransacaoCredito.CONSUMO);
        assertThat(t.getQuantidade()).isEqualTo(-1);
        assertThat(t.getSaldoApos()).isEqualTo(4);
        assertThat(conta.getSaldo()).isEqualTo(4);
    }

    @Test
    @DisplayName("CR-03: sem gratuitos e sem saldo — bloqueia o aceite e não registra transação")
    void consumirPorAceite_SemCreditos_LancaException() {
        when(contaRepository.findByPrestadorIdParaAtualizacao(10L)).thenReturn(Optional.of(conta(0, 3)));

        assertThatThrownBy(() -> creditoService.consumirPorAceite(proposta))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sem créditos");
        verify(transacaoRepository, never()).save(any());
    }

    // ── CR-04..06: validação ao enviar proposta ──────────────────────────────

    @Test
    @DisplayName("CR-04: prestador sem conta ainda pode enviar proposta (tier gratuito)")
    void validarCreditoParaProposta_SemConta_Permite() {
        when(contaRepository.findByPrestadorId(10L)).thenReturn(Optional.empty());

        assertThatCode(() -> creditoService.validarCreditoParaProposta(prestador)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CR-05: gratuitos esgotados e saldo zero — bloqueia envio de proposta")
    void validarCreditoParaProposta_SemCreditos_LancaException() {
        when(contaRepository.findByPrestadorId(10L)).thenReturn(Optional.of(conta(0, 3)));

        assertThatThrownBy(() -> creditoService.validarCreditoParaProposta(prestador))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Financeiro");
    }

    @Test
    @DisplayName("CR-06: gratuitos esgotados mas com saldo — permite envio de proposta")
    void validarCreditoParaProposta_ComSaldo_Permite() {
        when(contaRepository.findByPrestadorId(10L)).thenReturn(Optional.of(conta(2, 3)));

        assertThatCode(() -> creditoService.validarCreditoParaProposta(prestador)).doesNotThrowAnyException();
    }

    // ── CR-07..10: compra ────────────────────────────────────────────────────

    @Test
    @DisplayName("CR-07: compra aprovada na hora credita o saldo e registra o gasto")
    void comprar_GatewayAprova_CreditaSaldo() {
        ContaCredito conta = conta(1, 3);
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(pacoteRepository.findById(1L)).thenReturn(Optional.of(pacote));
        when(contaRepository.findByPrestadorId(10L)).thenReturn(Optional.of(conta));
        when(contaRepository.findByPrestadorIdParaAtualizacao(10L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPagamento.nome()).thenReturn("ficticio");
        when(gatewayPagamento.criarCobranca(any())).thenReturn(
                new GatewayPagamento.ResultadoCobranca("FICT-1", StatusTransacaoCredito.APROVADA, null));

        CompraCreditoDTO dto = creditoService.comprar(2L, 1L);

        assertThat(dto.getSaldoAtual()).isEqualTo(6);
        assertThat(dto.getTransacao().getStatus()).isEqualTo(StatusTransacaoCredito.APROVADA);
        assertThat(dto.getTransacao().getSaldoApos()).isEqualTo(6);
        assertThat(conta.getTotalGastoCentavos()).isEqualTo(1990L);
        assertThat(transacaoSalva().getReferenciaExterna()).isEqualTo("FICT-1");
    }

    @Test
    @DisplayName("CR-08: pacote inativo não pode ser comprado")
    void comprar_PacoteInativo_LancaNotFound() {
        pacote.setAtivo(false);
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(pacoteRepository.findById(1L)).thenReturn(Optional.of(pacote));

        assertThatThrownBy(() -> creditoService.comprar(2L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(gatewayPagamento, never()).criarCobranca(any());
    }

    @Test
    @DisplayName("CR-09: pagamento pendente não credita até a confirmação (fluxo de webhook)")
    void comprar_GatewayPendente_CreditaSoNaConfirmacao() {
        ContaCredito conta = conta(0, 3);
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(pacoteRepository.findById(1L)).thenReturn(Optional.of(pacote));
        when(contaRepository.findByPrestadorId(10L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(gatewayPagamento.nome()).thenReturn("mercadopago");
        when(gatewayPagamento.criarCobranca(any())).thenReturn(
                new GatewayPagamento.ResultadoCobranca("MP-123", StatusTransacaoCredito.PENDENTE, "https://checkout"));

        CompraCreditoDTO dto = creditoService.comprar(2L, 1L);

        assertThat(dto.getTransacao().getStatus()).isEqualTo(StatusTransacaoCredito.PENDENTE);
        assertThat(dto.getUrlCheckout()).isEqualTo("https://checkout");
        assertThat(conta.getSaldo()).isZero();

        TransacaoCredito pendente = transacaoSalva();
        when(transacaoRepository.findByReferenciaExterna("MP-123")).thenReturn(Optional.of(pendente));
        when(contaRepository.findByPrestadorIdParaAtualizacao(10L)).thenReturn(Optional.of(conta));

        creditoService.confirmarPagamento("MP-123", true);
        creditoService.confirmarPagamento("MP-123", true); // webhook repetido não credita de novo

        assertThat(conta.getSaldo()).isEqualTo(5);
        assertThat(pendente.getStatus()).isEqualTo(StatusTransacaoCredito.APROVADA);
    }

    @Test
    @DisplayName("CR-10: pagamento recusado não altera o saldo")
    void confirmarPagamento_Recusado_NaoCredita() {
        ContaCredito conta = conta(2, 3);
        TransacaoCredito pendente = new TransacaoCredito();
        pendente.setConta(conta);
        pendente.setStatus(StatusTransacaoCredito.PENDENTE);
        pendente.setQuantidade(5);
        pendente.setValorCentavos(1990L);
        when(transacaoRepository.findByReferenciaExterna("MP-9")).thenReturn(Optional.of(pendente));

        creditoService.confirmarPagamento("MP-9", false);

        assertThat(pendente.getStatus()).isEqualTo(StatusTransacaoCredito.RECUSADA);
        assertThat(conta.getSaldo()).isEqualTo(2);
        verify(contaRepository, never()).save(any());
    }

    // ── CR-11: resumo do painel financeiro ───────────────────────────────────

    @Test
    @DisplayName("CR-11: resumo indica saldo baixo e gratuitos restantes")
    void resumo_SaldoBaixo_SinalizaAlerta() {
        when(prestadorRepository.findByUsuarioId(2L)).thenReturn(Optional.of(prestador));
        when(contaRepository.findByPrestadorId(10L)).thenReturn(Optional.of(conta(2, 1)));
        when(transacaoRepository.findByContaIdOrderByDataCriacaoDescIdDesc(anyLong(), any()))
                .thenReturn(Page.empty());
        when(gatewayPagamento.ficticio()).thenReturn(true);

        ResumoCreditosDTO dto = creditoService.resumo(2L, PageRequest.of(0, 10));

        assertThat(dto.getSaldo()).isEqualTo(2);
        assertThat(dto.getSaldoBaixo()).isTrue();
        assertThat(dto.getGratuitosRestantes()).isEqualTo(2);
        assertThat(dto.getPagamentoFicticio()).isTrue();
    }
}
