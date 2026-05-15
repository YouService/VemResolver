import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PropostaService } from '../services/proposta.service';
import { SolicitacaoServico, Proposta, StatusProposta } from '../models/models';

@Component({
  selector: 'app-cliente-propostas-recebidas',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './cliente-propostas-recebidas.component.html'
})
export class ClientePropostasRecebidasComponent implements OnInit {
  solicitacao?: SolicitacaoServico;
  propostas: Proposta[] = [];
  StatusProposta = StatusProposta;

  loading = true;
  processando: number | null = null;
  idConfirmandoAceite: number | null = null;

  mensagemSucesso = '';
  mensagemErro = '';
  erroProposta: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private solicitacaoService: SolicitacaoService,
    private propostaService: PropostaService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarDados(id);
  }

  private carregarDados(solicitacaoId: number): void {
    this.loading = true;
    this.solicitacaoService.buscarPorId(solicitacaoId).subscribe({
      next: (s) => {
        this.solicitacao = s;
        this.propostaService.listarPorSolicitacao(solicitacaoId, 0, 50).subscribe({
          next: (page) => {
            this.propostas = page.content;
            this.loading = false;
          },
          error: () => { this.loading = false; }
        });
      },
      error: () => { this.loading = false; }
    });
  }

  confirmarAceite(propostaId: number): void {
    this.idConfirmandoAceite = propostaId;
    this.erroProposta = null;
  }

  cancelarConfirmacao(): void {
    this.idConfirmandoAceite = null;
  }

  aceitarProposta(propostaId: number): void {
    this.processando = propostaId;
    this.erroProposta = null;

    this.propostaService.aceitar(propostaId).subscribe({
      next: () => {
        this.processando = null;
        this.idConfirmandoAceite = null;
        this.mensagemSucesso = 'Proposta aceita! O prestador foi selecionado e a solicitação está Em Andamento.';
        this.carregarDados(this.solicitacao!.id!);
      },
      error: (err) => {
        this.processando = null;
        this.idConfirmandoAceite = null;
        this.erroProposta = propostaId;
        this.mensagemErro = err.error?.message || 'Erro ao aceitar proposta. Tente novamente.';
      }
    });
  }

  recusarProposta(propostaId: number): void {
    this.processando = propostaId;
    this.erroProposta = null;

    this.propostaService.recusar(propostaId).subscribe({
      next: () => {
        this.processando = null;
        this.carregarDados(this.solicitacao!.id!);
      },
      error: (err) => {
        this.processando = null;
        this.erroProposta = propostaId;
        this.mensagemErro = err.error?.message || 'Erro ao recusar proposta. Tente novamente.';
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/cliente/dashboard']);
  }

  formatarValor(valor: number): string {
    return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatarData(data: any): string {
    if (!data) return '';
    const date = new Date(data);
    const hoje = new Date();
    const diff = hoje.getTime() - date.getTime();
    const dias = Math.floor(diff / (1000 * 60 * 60 * 24));
    if (dias === 0) return 'Hoje';
    if (dias === 1) return 'Ontem';
    if (dias < 7) return `${dias} dias atrás`;
    return date.toLocaleDateString('pt-BR');
  }

  getStatusPropostaClass(status: StatusProposta): string {
    const classes: Record<string, string> = {
      [StatusProposta.ENVIADA]:  'bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full',
      [StatusProposta.ACEITA]:   'bg-green-100 text-green-800 text-xs font-semibold px-3 py-1 rounded-full',
      [StatusProposta.RECUSADA]: 'bg-gray-100 text-gray-600 text-xs font-semibold px-3 py-1 rounded-full'
    };
    return classes[status] ?? 'bg-gray-100 text-gray-800 text-xs font-semibold px-3 py-1 rounded-full';
  }

  getStatusPropostaText(status: StatusProposta): string {
    const texts: Record<string, string> = {
      [StatusProposta.ENVIADA]:  'Aguardando',
      [StatusProposta.ACEITA]:   '✓ Aceita',
      [StatusProposta.RECUSADA]: 'Recusada'
    };
    return texts[status] ?? status;
  }

  getStatusSolicitacaoClass(status: string): string {
    const classes: Record<string, string> = {
      'ABERTA':       'bg-green-100 text-green-800 text-xs font-semibold px-3 py-1 rounded-full',
      'EM_ANDAMENTO': 'bg-yellow-100 text-yellow-800 text-xs font-semibold px-3 py-1 rounded-full',
      'CONCLUIDA':    'bg-purple-100 text-purple-800 text-xs font-semibold px-3 py-1 rounded-full',
      'CANCELADA':    'bg-red-100 text-red-800 text-xs font-semibold px-3 py-1 rounded-full'
    };
    return classes[status] ?? 'bg-gray-100 text-gray-800 text-xs font-semibold px-3 py-1 rounded-full';
  }

  getStatusSolicitacaoText(status: string): string {
    const texts: Record<string, string> = {
      'ABERTA':       'Aberta',
      'EM_ANDAMENTO': 'Em Andamento',
      'CONCLUIDA':    'Concluída',
      'CANCELADA':    'Cancelada'
    };
    return texts[status] ?? status;
  }
}
