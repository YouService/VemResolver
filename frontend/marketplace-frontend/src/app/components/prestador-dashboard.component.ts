import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PrestadorService } from '../services/prestador.service';
import { PropostaService } from '../services/proposta.service';
import { AuthService } from '../services/auth.service';
import { SolicitacaoServico, Prestador, Proposta, StatusProposta, StatusSolicitacao } from '../models/models';

@Component({
  selector: 'app-prestador-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './prestador-dashboard.component.html'
})
export class PrestadorDashboardComponent implements OnInit {
  solicitacoes: SolicitacaoServico[] = [];
  minhasPropostas: Proposta[] = [];
  prestador?: Prestador;
  userName = '';
  loading = false;
  paginaAtual = 0;
  totalPaginas = 0;
  itensPorPagina = 9;

  constructor(
    private solicitacaoService: SolicitacaoService,
    private prestadorService: PrestadorService,
    private propostaService: PropostaService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userName = this.authService.getCurrentUserName() || 'Prestador';
    this.carregarPerfil();
    this.carregarSolicitacoes();
    this.carregarPropostas();
  }

  get contratosAndamento(): Proposta[] {
    return this.minhasPropostas.filter(
      p => p.status === StatusProposta.ACEITA &&
           p.solicitacaoStatus === StatusSolicitacao.EM_ANDAMENTO
    );
  }

  get propostasAguardando(): Proposta[] {
    return this.minhasPropostas.filter(p => p.status === StatusProposta.ENVIADA);
  }

  get servicosConcluidos(): Proposta[] {
    return this.minhasPropostas.filter(
      p => p.solicitacaoStatus === StatusSolicitacao.CONCLUIDA
    );
  }

  carregarPerfil() {
    this.prestadorService.buscarMeuPerfil().subscribe({
      next: (prestador: Prestador) => { this.prestador = prestador; },
      error: () => {}
    });
  }

  carregarSolicitacoes(pagina: number = this.paginaAtual) {
    this.loading = true;
    this.solicitacaoService.buscarAbertas(undefined, undefined, undefined, pagina, this.itensPorPagina).subscribe({
      next: (response) => {
        this.solicitacoes = response.content;
        this.totalPaginas = response.totalPages;
        this.paginaAtual = pagina;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  irParaPagina(pagina: number) {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.carregarSolicitacoes(pagina);
    }
  }

  carregarPropostas() {
    this.propostaService.listarMinhas(0, 50).subscribe({
      next: (response) => { this.minhasPropostas = response.content; },
      error: () => {}
    });
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
}
