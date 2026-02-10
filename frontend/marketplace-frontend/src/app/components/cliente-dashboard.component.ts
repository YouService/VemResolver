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
  template: `
    <div class="min-h-screen bg-gray-50">
      <!-- Header -->
      <div class="bg-white shadow-sm border-b">
        <div class="container mx-auto px-4 py-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center justify-between">
              <h1 class="text-3xl font-bold text-gray-900">Painel do Cliente</h1>
              <p class="text-gray-600 mt-1">Bem-vindo, {{ userName }}</p>
            </div>
            <div class="flex gap-3">
              <button
                routerLink="/cliente/nova-solicitacao"
                class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition">
                + Nova Solicitação
              </button>
              <button
                routerLink="/cliente/perfil"
                class="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition">
                Meu Perfil
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="container mx-auto px-4 py-8">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Total de Solicitações</p>
                <p class="text-3xl font-bold text-blue-600 mt-2">{{ solicitacoes.length }}</p>
              </div>
              <div class="text-4xl">📋</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Abertas</p>
                <p class="text-3xl font-bold text-green-600 mt-2">{{ contarPorStatus('ABERTA') }}</p>
              </div>
              <div class="text-4xl">🟢</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Em Andamento</p>
                <p class="text-3xl font-bold text-yellow-600 mt-2">{{ contarPorStatus('EM_ANDAMENTO') }}</p>
              </div>
              <div class="text-4xl">🟡</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Concluídas</p>
                <p class="text-3xl font-bold text-purple-600 mt-2">{{ contarPorStatus('CONCLUIDA') }}</p>
              </div>
              <div class="text-4xl">✅</div>
            </div>
          </div>
        </div>

        <!-- Minhas Solicitações -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-gray-900">Minhas Solicitações</h2>
            <div class="flex gap-3">
              <select 
                [(ngModel)]="filtroStatus"
                (change)="aplicarFiltro()"
                class="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
                <option value="">Todos os Status</option>
                <option value="ABERTA">Abertas</option>
                <option value="EM_ANDAMENTO">Em Andamento</option>
                <option value="CONCLUIDA">Concluídas</option>
                <option value="CANCELADA">Canceladas</option>
              </select>
              <button 
                (click)="carregarSolicitacoes()"
                class="text-blue-600 hover:text-blue-700 font-medium">
                🔄 Atualizar
              </button>
            </div>
          </div>

          @if (loading) {
            <div class="text-center py-12">
              <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
              <p class="text-gray-600 mt-4">Carregando solicitações...</p>
            </div>
          } @else if (solicitacoesFiltradas.length === 0) {
            <div class="text-center py-12">
              <span class="text-6xl">📭</span>
              <p class="text-gray-600 mt-4 text-lg">Nenhuma solicitação encontrada</p>
              <button 
                routerLink="/cliente/nova-solicitacao"
                class="mt-4 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
                Criar Primeira Solicitação
              </button>
            </div>
          } @else {
            <div class="space-y-4">
              @for (solicitacao of solicitacoesFiltradas; track solicitacao.id) {
                <div class="border rounded-lg p-5 hover:shadow-lg transition">
                  <div class="flex items-start justify-between mb-3">
                    <div class="flex-1">
                      <div class="flex items-center gap-3 mb-2">
                        <span class="bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full">
                          {{ solicitacao.categoriaNome }}
                        </span>
                        <span [class]="getStatusClass(solicitacao.status!)">
                          {{ getStatusText(solicitacao.status!) }}
                        </span>
                      </div>
                      <p class="text-gray-800 mb-2">{{ solicitacao.descricao }}</p>
                    </div>
                    <span class="text-xs text-gray-500 ml-4">
                      {{ formatarData(solicitacao.dataCriacao) }}
                    </span>
                  </div>
                  
                  <div class="flex items-center justify-between">
                    <div class="flex items-center text-sm text-gray-600">
                      <span class="mr-4">📍 {{ solicitacao.cidade }}, {{ solicitacao.estado }}</span>
                    </div>

                    <button
                      (click)="verDetalhes(solicitacao.id!)"
                      class="px-4 py-2 text-blue-600 border border-blue-600 rounded-lg hover:bg-blue-50 transition font-medium">
                      Ver Detalhes
                    </button>
                  </div>
                </div>
              }
            </div>
          }
        </div>
      </div>

      @if (solicitacaoSelecionada) {
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" (click)="fecharModal()">
          <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto" (click)="$event.stopPropagation()">
            <div class="p-6 border-b">
              <div class="flex items-center justify-between">
                <h2 class="text-2xl font-bold text-gray-900">Detalhes da Solicitação</h2>
                <button (click)="fecharModal()" class="text-gray-400 hover:text-gray-600">
                  ✕
                </button>
              </div>
            </div>
            <div class="p-6">
              <div class="space-y-4">
                <div class="flex items-center gap-2">
                  <span class="bg-blue-100 text-blue-800 text-sm font-semibold px-3 py-1 rounded-full">
                    {{ solicitacaoSelecionada.categoriaNome }}
                  </span>
                  <span [class]="getStatusClass(solicitacaoSelecionada.status!)">
                    {{ getStatusText(solicitacaoSelecionada.status!) }}
                  </span>
                </div>
                <div>
                  <p class="text-sm text-gray-600">Descrição</p>
                  <p class="text-gray-800 mt-1">{{ solicitacaoSelecionada.descricao }}</p>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-sm text-gray-600">Cidade</p>
                    <p class="text-gray-800 mt-1">{{ solicitacaoSelecionada.cidade }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-600">Estado</p>
                    <p class="text-gray-800 mt-1">{{ solicitacaoSelecionada.estado }}</p>
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <p class="text-sm text-gray-600">Data de Criação</p>
                    <p class="text-gray-800 mt-1">{{ formatarDataCompleta(solicitacaoSelecionada.dataCriacao) }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-gray-600">Status</p>
                    <p class="text-gray-800 mt-1">{{ getStatusText(solicitacaoSelecionada.status!) }}</p>
                  </div>
                </div>
              </div>
            </div>
            @if (podeEditarOuExcluir()) {
              <div class="p-6 border-t bg-gray-50 flex gap-3 justify-end">
                <button
                  (click)="confirmarExclusao()"
                  class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium">
                  Excluir
                </button>
                <button
                  (click)="editarSolicitacao()"
                  class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium">
                  Editar
                </button>
              </div>
            }
          </div>
        </div>
      }

      @if (mostrarModalExclusao) {
        <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" (click)="fecharModalExclusao()">
          <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4" (click)="$event.stopPropagation()">
            <div class="p-6">
              <h3 class="text-xl font-bold text-gray-900 mb-4">Confirmar Exclusão</h3>
              <p class="text-gray-600">Tem certeza que deseja excluir esta solicitação? Esta ação não pode ser desfeita.</p>
            </div>
            <div class="p-6 border-t bg-gray-50 flex gap-3 justify-end">
              <button
                (click)="fecharModalExclusao()"
                class="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400 transition font-medium">
                Cancelar
              </button>
              <button
                (click)="excluirSolicitacao()"
                class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium">
                {{ excluindo ? 'Excluindo...' : 'Excluir' }}
              </button>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class ClienteDashboardComponent implements OnInit {
  solicitacoes: SolicitacaoServico[] = [];
  solicitacoesFiltradas: SolicitacaoServico[] = [];
  userName: string = '';
  loading = false;
  filtroStatus: string = '';
  solicitacaoSelecionada: SolicitacaoServico | null = null;
  mostrarModalExclusao = false;
  excluindo = false;
  errorMessage = '';

  constructor(
    private solicitacaoService: SolicitacaoService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userName = this.authService.getCurrentUserName() || 'Cliente';
    this.carregarSolicitacoes();
  }

  carregarSolicitacoes() {
    this.loading = true;
    this.solicitacaoService.listarMinhas().subscribe({
      next: (response) => {
        this.solicitacoes = response.content;
        this.aplicarFiltro();
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar solicitações', error);
        this.loading = false;
      }
    });
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
      error: (error: any) => {
        console.error('Erro ao carregar detalhes da solicitação', error);
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
      this.router.navigate(['/cliente/editar-solicitacao', this.solicitacaoSelecionada.id]);
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
}
