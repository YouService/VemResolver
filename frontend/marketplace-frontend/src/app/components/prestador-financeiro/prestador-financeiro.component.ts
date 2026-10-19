import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CreditoService } from '../../services/credito.service';
import {
  PacoteCredito,
  ResumoCreditos,
  StatusTransacaoCredito,
  TipoTransacaoCredito,
  TransacaoCredito
} from '../../models/models';

@Component({
  selector: 'app-prestador-financeiro',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './prestador-financeiro.component.html'
})
export class PrestadorFinanceiroComponent implements OnInit {
  resumo?: ResumoCreditos;
  pacotes: PacoteCredito[] = [];
  loading = true;
  erro = '';
  sucesso = '';
  comprandoId?: number;
  paginaAtual = 0;
  readonly itensPorPagina = 10;

  constructor(private creditoService: CreditoService) {}

  ngOnInit(): void {
    this.carregarResumo();
    this.creditoService.pacotes().subscribe({
      next: pacotes => this.pacotes = pacotes,
      error: () => this.erro = 'Não foi possível carregar os pacotes de créditos.'
    });
  }

  carregarResumo(pagina: number = this.paginaAtual): void {
    this.creditoService.resumo(pagina, this.itensPorPagina).subscribe({
      next: resumo => {
        this.resumo = resumo;
        this.paginaAtual = pagina;
        this.loading = false;
      },
      error: () => {
        this.erro = 'Não foi possível carregar seu saldo de créditos.';
        this.loading = false;
      }
    });
  }

  comprar(pacote: PacoteCredito): void {
    this.erro = '';
    this.sucesso = '';
    this.comprandoId = pacote.id;
    this.creditoService.comprar(pacote.id).subscribe({
      next: compra => {
        this.comprandoId = undefined;
        if (compra.urlCheckout) {
          window.location.href = compra.urlCheckout;
          return;
        }
        if (compra.transacao.status === StatusTransacaoCredito.APROVADA) {
          this.sucesso = `${pacote.quantidadeCreditos} créditos adicionados! Saldo atual: ${compra.saldoAtual}.`;
        } else if (compra.transacao.status === StatusTransacaoCredito.PENDENTE) {
          this.sucesso = 'Pagamento em processamento. Os créditos entram assim que for confirmado.';
        } else {
          this.erro = 'Pagamento recusado. Tente novamente ou use outra forma de pagamento.';
        }
        this.carregarResumo(0);
      },
      error: err => {
        this.comprandoId = undefined;
        this.erro = err.error?.message || 'Erro ao comprar créditos. Tente novamente.';
      }
    });
  }

  formatarReais(centavos?: number | null): string {
    return ((centavos ?? 0) / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  precoPorCredito(pacote: PacoteCredito): string {
    return this.formatarReais(Math.round(pacote.precoCentavos / pacote.quantidadeCreditos));
  }

  rotuloTipo(t: TransacaoCredito): string {
    switch (t.tipo) {
      case TipoTransacaoCredito.COMPRA: return 'Compra';
      case TipoTransacaoCredito.CONSUMO: return 'Proposta aceita';
      case TipoTransacaoCredito.CONSUMO_GRATUITO: return 'Aceite gratuito';
    }
  }

  rotuloStatus(status: StatusTransacaoCredito): string {
    switch (status) {
      case StatusTransacaoCredito.APROVADA: return 'Aprovada';
      case StatusTransacaoCredito.PENDENTE: return 'Pendente';
      case StatusTransacaoCredito.RECUSADA: return 'Recusada';
    }
  }

  classeStatus(status: StatusTransacaoCredito): string {
    switch (status) {
      case StatusTransacaoCredito.APROVADA: return 'bg-green-100 text-green-800';
      case StatusTransacaoCredito.PENDENTE: return 'bg-yellow-100 text-yellow-800';
      case StatusTransacaoCredito.RECUSADA: return 'bg-red-100 text-red-800';
    }
  }

  classeQuantidade(t: TransacaoCredito): string {
    if (t.quantidade > 0) return 'text-green-700';
    if (t.quantidade < 0) return 'text-red-700';
    return 'text-gray-500';
  }

  formatarQuantidade(t: TransacaoCredito): string {
    return t.quantidade > 0 ? `+${t.quantidade}` : `${t.quantidade}`;
  }
}
