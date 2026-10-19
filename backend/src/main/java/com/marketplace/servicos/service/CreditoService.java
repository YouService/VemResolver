package com.marketplace.servicos.service;

import com.marketplace.servicos.dto.CompraCreditoDTO;
import com.marketplace.servicos.dto.PacoteCreditoDTO;
import com.marketplace.servicos.dto.ResumoCreditosDTO;
import com.marketplace.servicos.dto.TransacaoCreditoDTO;
import com.marketplace.servicos.exception.BusinessException;
import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.*;
import com.marketplace.servicos.repository.ContaCreditoRepository;
import com.marketplace.servicos.repository.PacoteCreditoRepository;
import com.marketplace.servicos.repository.PrestadorRepository;
import com.marketplace.servicos.repository.TransacaoCreditoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Créditos pré-pagos do prestador (P6.1): cada proposta aceita consome 1 crédito,
 * exceto os primeiros aceites cobertos pelo tier gratuito.
 */
@Service
public class CreditoService {

    private static final Logger log = LoggerFactory.getLogger(CreditoService.class);

    static final int CREDITOS_POR_ACEITE = 1;

    private final ContaCreditoRepository contaRepository;
    private final TransacaoCreditoRepository transacaoRepository;
    private final PacoteCreditoRepository pacoteRepository;
    private final PrestadorRepository prestadorRepository;
    private final GatewayPagamento gatewayPagamento;

    @Value("${creditos.gratuitos:3}")
    private int creditosGratuitos = 3;

    @Value("${creditos.alerta-saldo-baixo:3}")
    private int limiteAlertaSaldo = 3;

    public CreditoService(ContaCreditoRepository contaRepository,
                          TransacaoCreditoRepository transacaoRepository,
                          PacoteCreditoRepository pacoteRepository,
                          PrestadorRepository prestadorRepository,
                          GatewayPagamento gatewayPagamento) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.pacoteRepository = pacoteRepository;
        this.prestadorRepository = prestadorRepository;
        this.gatewayPagamento = gatewayPagamento;
    }

    // ── Regras de consumo ────────────────────────────────────────────────────

    /** Impede o prestador de enviar proposta que não poderia ser aceita por falta de créditos. */
    @Transactional(readOnly = true)
    public void validarCreditoParaProposta(Prestador prestador) {
        ContaCredito conta = contaRepository.findByPrestadorId(prestador.getId()).orElse(null);
        if (!temCreditoDisponivel(conta)) {
            throw new BusinessException("Você não tem créditos disponíveis. Compre um pacote na aba Financeiro para enviar novas propostas.");
        }
    }

    /** Debita o aceite de uma proposta: usa o tier gratuito primeiro, depois o saldo. */
    @Transactional
    public void consumirPorAceite(Proposta proposta) {
        ContaCredito conta = contaParaAtualizacao(proposta.getPrestador());

        TransacaoCredito transacao = new TransacaoCredito();
        transacao.setConta(conta);
        transacao.setProposta(proposta);
        transacao.setStatus(StatusTransacaoCredito.APROVADA);
        transacao.setDataConfirmacao(LocalDateTime.now().withNano(0));

        if (conta.getGratuitosUsados() < creditosGratuitos) {
            conta.setGratuitosUsados(conta.getGratuitosUsados() + 1);
            transacao.setTipo(TipoTransacaoCredito.CONSUMO_GRATUITO);
            transacao.setQuantidade(0);
            transacao.setDescricao("Aceite gratuito " + conta.getGratuitosUsados() + " de " + creditosGratuitos
                    + " — proposta #" + proposta.getId());
        } else if (conta.getSaldo() >= CREDITOS_POR_ACEITE) {
            conta.setSaldo(conta.getSaldo() - CREDITOS_POR_ACEITE);
            transacao.setTipo(TipoTransacaoCredito.CONSUMO);
            transacao.setQuantidade(-CREDITOS_POR_ACEITE);
            transacao.setDescricao("Proposta #" + proposta.getId() + " aceita");
        } else {
            throw new BusinessException("Este prestador está sem créditos no momento e não pode assumir novos serviços. Escolha outra proposta ou tente mais tarde.");
        }

        transacao.setSaldoApos(conta.getSaldo());
        contaRepository.save(conta);
        transacaoRepository.save(transacao);
        log.info("Credito consumido prestador={} proposta={} tipo={} saldo={}",
                proposta.getPrestador().getId(), proposta.getId(), transacao.getTipo(), conta.getSaldo());
    }

    // ── Painel financeiro (P6.3) ─────────────────────────────────────────────

    @Transactional
    public ResumoCreditosDTO resumo(Long usuarioId, Pageable pageable) {
        ContaCredito conta = obterOuCriarConta(buscarPrestador(usuarioId));

        ResumoCreditosDTO dto = new ResumoCreditosDTO();
        dto.setSaldo(conta.getSaldo());
        dto.setGratuitosTotal(creditosGratuitos);
        dto.setGratuitosRestantes(Math.max(0, creditosGratuitos - conta.getGratuitosUsados()));
        dto.setTotalGastoCentavos(conta.getTotalGastoCentavos());
        dto.setLimiteAlertaSaldo(limiteAlertaSaldo);
        dto.setSaldoBaixo(conta.getSaldo() < limiteAlertaSaldo);
        dto.setPagamentoFicticio(gatewayPagamento.ficticio());
        dto.setTransacoes(transacaoRepository
                .findByContaIdOrderByDataCriacaoDescIdDesc(conta.getId(), pageable)
                .map(this::toTransacaoDTO));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PacoteCreditoDTO> listarPacotesAtivos() {
        return pacoteRepository.findByAtivoTrueOrderByPrecoCentavosAsc().stream().map(this::toPacoteDTO).toList();
    }

    // ── Compra ───────────────────────────────────────────────────────────────

    @Transactional
    public CompraCreditoDTO comprar(Long usuarioId, Long pacoteId) {
        Prestador prestador = buscarPrestador(usuarioId);
        PacoteCredito pacote = pacoteRepository.findById(pacoteId)
                .filter(PacoteCredito::getAtivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pacote não encontrado"));
        ContaCredito conta = obterOuCriarConta(prestador);

        TransacaoCredito transacao = new TransacaoCredito();
        transacao.setConta(conta);
        transacao.setPacote(pacote);
        transacao.setTipo(TipoTransacaoCredito.COMPRA);
        transacao.setStatus(StatusTransacaoCredito.PENDENTE);
        transacao.setQuantidade(pacote.getQuantidadeCreditos());
        transacao.setValorCentavos(pacote.getPrecoCentavos());
        transacao.setDescricao("Compra do pacote " + pacote.getNome());
        transacao.setGateway(gatewayPagamento.nome());
        transacao = transacaoRepository.save(transacao);

        GatewayPagamento.ResultadoCobranca resultado = gatewayPagamento.criarCobranca(new GatewayPagamento.Cobranca(
                transacao.getId(), transacao.getDescricao(), pacote.getPrecoCentavos(),
                prestador.getUsuario().getEmail()));
        transacao.setReferenciaExterna(resultado.referenciaExterna());

        if (resultado.status() == StatusTransacaoCredito.APROVADA) {
            aprovar(transacao);
        } else if (resultado.status() == StatusTransacaoCredito.RECUSADA) {
            transacao.setStatus(StatusTransacaoCredito.RECUSADA);
            transacaoRepository.save(transacao);
        } else {
            transacaoRepository.save(transacao);
        }

        CompraCreditoDTO dto = new CompraCreditoDTO();
        dto.setTransacao(toTransacaoDTO(transacao));
        dto.setSaldoAtual(transacao.getConta().getSaldo());
        dto.setUrlCheckout(resultado.urlCheckout());
        return dto;
    }

    /**
     * Confirma uma compra pendente a partir da referência do provedor.
     * Ponto de entrada para o webhook do gateway real; é idempotente.
     */
    @Transactional
    public void confirmarPagamento(String referenciaExterna, boolean aprovado) {
        TransacaoCredito transacao = transacaoRepository.findByReferenciaExterna(referenciaExterna)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada"));
        if (transacao.getStatus() != StatusTransacaoCredito.PENDENTE) {
            return;
        }
        if (aprovado) {
            aprovar(transacao);
        } else {
            transacao.setStatus(StatusTransacaoCredito.RECUSADA);
            transacaoRepository.save(transacao);
        }
    }

    // ── Administração de pacotes ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PacoteCreditoDTO> listarTodosPacotes() {
        return pacoteRepository.findAllByOrderByPrecoCentavosAsc().stream().map(this::toPacoteDTO).toList();
    }

    @Transactional
    public PacoteCreditoDTO salvarPacote(Long id, PacoteCreditoDTO dto) {
        PacoteCredito pacote = (id == null)
                ? new PacoteCredito()
                : pacoteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pacote não encontrado"));
        pacote.setNome(dto.getNome());
        pacote.setDescricao(dto.getDescricao());
        pacote.setQuantidadeCreditos(dto.getQuantidadeCreditos());
        pacote.setPrecoCentavos(dto.getPrecoCentavos());
        pacote.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        return toPacoteDTO(pacoteRepository.save(pacote));
    }

    // ── Internos ─────────────────────────────────────────────────────────────

    private void aprovar(TransacaoCredito transacao) {
        ContaCredito conta = contaParaAtualizacao(transacao.getConta().getPrestador());
        conta.setSaldo(conta.getSaldo() + transacao.getQuantidade());
        conta.setTotalGastoCentavos(conta.getTotalGastoCentavos() + transacao.getValorCentavos());
        contaRepository.save(conta);

        transacao.setConta(conta);
        transacao.setStatus(StatusTransacaoCredito.APROVADA);
        transacao.setSaldoApos(conta.getSaldo());
        transacao.setDataConfirmacao(LocalDateTime.now().withNano(0));
        transacaoRepository.save(transacao);
        log.info("Compra de creditos aprovada transacao={} prestador={} +{} saldo={}",
                transacao.getId(), conta.getPrestador().getId(), transacao.getQuantidade(), conta.getSaldo());
    }

    private boolean temCreditoDisponivel(ContaCredito conta) {
        if (conta == null) {
            return creditosGratuitos > 0;
        }
        return conta.getGratuitosUsados() < creditosGratuitos || conta.getSaldo() >= CREDITOS_POR_ACEITE;
    }

    private Prestador buscarPrestador(Long usuarioId) {
        return prestadorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestador não encontrado"));
    }

    private ContaCredito obterOuCriarConta(Prestador prestador) {
        return contaRepository.findByPrestadorId(prestador.getId())
                .orElseGet(() -> novaConta(prestador));
    }

    private ContaCredito contaParaAtualizacao(Prestador prestador) {
        return contaRepository.findByPrestadorIdParaAtualizacao(prestador.getId())
                .orElseGet(() -> novaConta(prestador));
    }

    private ContaCredito novaConta(Prestador prestador) {
        ContaCredito conta = new ContaCredito();
        conta.setPrestador(prestador);
        return contaRepository.save(conta);
    }

    private TransacaoCreditoDTO toTransacaoDTO(TransacaoCredito t) {
        TransacaoCreditoDTO dto = new TransacaoCreditoDTO();
        dto.setId(t.getId());
        dto.setTipo(t.getTipo());
        dto.setStatus(t.getStatus());
        dto.setQuantidade(t.getQuantidade());
        dto.setValorCentavos(t.getValorCentavos());
        dto.setSaldoApos(t.getSaldoApos());
        dto.setDescricao(t.getDescricao());
        dto.setPacoteNome(t.getPacote() != null ? t.getPacote().getNome() : null);
        dto.setPropostaId(t.getProposta() != null ? t.getProposta().getId() : null);
        dto.setGateway(t.getGateway());
        dto.setDataCriacao(t.getDataCriacao());
        dto.setDataConfirmacao(t.getDataConfirmacao());
        return dto;
    }

    private PacoteCreditoDTO toPacoteDTO(PacoteCredito p) {
        PacoteCreditoDTO dto = new PacoteCreditoDTO();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setDescricao(p.getDescricao());
        dto.setQuantidadeCreditos(p.getQuantidadeCreditos());
        dto.setPrecoCentavos(p.getPrecoCentavos());
        dto.setAtivo(p.getAtivo());
        return dto;
    }
}
