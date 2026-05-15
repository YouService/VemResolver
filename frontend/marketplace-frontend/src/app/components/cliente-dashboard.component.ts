import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { AuthService } from '../services/auth.service';
import { SolicitacaoServico, StatusSolicitacao } from '../models/models';

@Component({
  selector: 'app-cliente-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './cliente-dashboard.component.html'
})
export class ClienteDashboardComponent implements OnInit {
  solicitacoes: SolicitacaoServico[] = [];
  solicitacoesFiltradas: SolicitacaoServico[] = [];
  userName: string = '';
  loading = false;
  minhaAvaliacaoMedia = 0;
  totalAvaliacoesRecebidas = 0;
  filtroStatus: string = '';
  solicitacaoSelecionada: SolicitacaoServico | null = null;
  mostrarModalExclusao = false;
  excluindo = false;
  errorMessage = '';
  mostrarModalConclusao = false;
  solicitacaoParaConcluir: SolicitacaoServico | null = null;
  concluindo = false;
  mensagemSucesso = '';
  idSolicitacaoConcluida: number | null = null;
  paginaAtual = 0;
  totalPaginas = 0;
  itensPorPagina = 10;

  constructor(
    private solicitacaoService: SolicitacaoService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userName = this.authService.getCurrentUserName() || 'Cliente';
    this.carregarSolicitacoes();
    this.carregarMinhaAvaliacao();
  }

  carregarMinhaAvaliacao(): void {
    this.authService.buscarPerfil().subscribe({
      next: (user) => {
        this.minhaAvaliacaoMedia = user.avaliacaoMedia || 0;
        this.totalAvaliacoesRecebidas = user.totalAvaliacoes || 0;
      },
      error: () => {}
    });
  }

  carregarSolicitacoes(pagina: number = this.paginaAtual) {
    this.loading = true;
    this.errorMessage = '';
    this.solicitacaoService.listarMinhas(pagina, this.itensPorPagina).subscribe({
      next: (response) => {
        this.solicitacoes = response.content;
        this.totalPaginas = response.totalPages;
        this.paginaAtual = pagina;
        this.aplicarFiltro();
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar suas solicitações. Tente atualizar a página.';
        this.loading = false;
      }
    });
  }

  irParaPagina(pagina: number) {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.carregarSolicitacoes(pagina);
    }
  }

  aplicarFiltro() {
    if (!this.filtroStatus) {
      this.solicitacoesFiltradas = this.solicitacoes;
    } else {
      this.solicitacoesFiltradas = this.solicitacoes.filter(
        s => s.status === this.filtroStatus
      );
    }
  }

  contarPorStatus(status: string): number {
    return this.solicitacoes.filter(s => s.status === status).length;
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'ABERTA': 'bg-green-100 text-green-800 text-xs font-semibold px-3 py-1 rounded-full',
      'EM_ANDAMENTO': 'bg-yellow-100 text-yellow-800 text-xs font-semibold px-3 py-1 rounded-full',
      'CONCLUIDA': 'bg-purple-100 text-purple-800 text-xs font-semibold px-3 py-1 rounded-full',
      'CANCELADA': 'bg-red-100 text-red-800 text-xs font-semibold px-3 py-1 rounded-full'
    };
    return classes[status] || 'bg-gray-100 text-gray-800 text-xs font-semibold px-3 py-1 rounded-full';
  }

  getStatusText(status: string): string {
    const texts: { [key: string]: string } = {
      'ABERTA': 'Aberta',
      'EM_ANDAMENTO': 'Em Andamento',
      'CONCLUIDA': 'Concluída',
      'CANCELADA': 'Cancelada'
    };
    return texts[status] || status;
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

  formatarDataCompleta(data: any): string {
    if (!data) return '';
    const date = new Date(data);
    return date.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  verDetalhes(id: number) {
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (solicitacao) => {
        this.solicitacaoSelecionada = solicitacao;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar detalhes. Tente novamente.';
      }
    });
  }

  fecharModal() {
    this.solicitacaoSelecionada = null;
    this.errorMessage = '';
  }

  podeEditarOuExcluir(): boolean {
    return this.solicitacaoSelecionada?.status === 'ABERTA';
  }

  editarSolicitacao() {
    if (this.solicitacaoSelecionada) {
      const id = this.solicitacaoSelecionada.id;
      this.fecharModal();
      this.router.navigate(['/cliente/editar-solicitacao', id]);
    }
  }

  confirmarExclusao() {
    this.mostrarModalExclusao = true;
  }

  fecharModalExclusao() {
    this.mostrarModalExclusao = false;
  }

  excluirSolicitacao() {
    if (!this.solicitacaoSelecionada) return;

    this.excluindo = true;
    this.solicitacaoService.deletar(this.solicitacaoSelecionada.id!).subscribe({
      next: () => {
        this.fecharModal();
        this.fecharModalExclusao();
        this.carregarSolicitacoes();
        this.excluindo = false;
      },
      error: (error: any) => {
        console.error('Erro ao excluir solicitação', error);
        this.errorMessage = 'Erro ao excluir solicitação';
        this.excluindo = false;
      }
    });
  }

  confirmarConclusao(solicitacao: SolicitacaoServico) {
    this.solicitacaoParaConcluir = solicitacao;
    this.mostrarModalConclusao = true;
  }

  fecharModalConclusao() {
    this.mostrarModalConclusao = false;
    this.solicitacaoParaConcluir = null;
  }

  executarConclusao() {
    if (!this.solicitacaoParaConcluir) return;

    this.concluindo = true;
    this.solicitacaoService.concluir(this.solicitacaoParaConcluir.id!).subscribe({
      next: () => {
        const idConcluida = this.solicitacaoParaConcluir!.id!;
        this.fecharModalConclusao();
        this.fecharModal();
        this.carregarSolicitacoes();
        this.concluindo = false;
        this.idSolicitacaoConcluida = idConcluida;
        this.mensagemSucesso = 'Serviço marcado como concluído! Avalie o prestador.';
        setTimeout(() => { this.mensagemSucesso = ''; this.idSolicitacaoConcluida = null; }, 8000);
      },
      error: () => {
        this.errorMessage = 'Erro ao concluir serviço. Tente novamente.';
        this.concluindo = false;
      }
    });
  }
}
