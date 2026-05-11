import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
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
            <div>
              <h1 class="text-3xl font-bold text-gray-900">Painel do Cliente</h1>
              <p class="text-gray-600 mt-1">Bem-vindo, {{ userName }}</p>
            </div>
            <button 
              routerLink="/cliente/nova-solicitacao"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition">
              + Nova Solicitação
            </button>
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

                    <div class="flex gap-2">
                      <a
                        [routerLink]="['/cliente/solicitacao', solicitacao.id, 'propostas']"
                        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium text-sm">
                        Ver Propostas
                      </a>
                      <button
                        (click)="verDetalhes(solicitacao.id!)"
                        class="px-4 py-2 text-blue-600 border border-blue-600 rounded-lg hover:bg-blue-50 transition font-medium text-sm">
                        Detalhes
                      </button>
                    </div>
                  </div>
                </div>
              }
            </div>
          }
        <!-- Modal de Detalhes -->
        @if (solicitacaoSelecionada) {
          <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" (click)="fecharModal()">
            <div class="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto mx-4" (click)="$event.stopPropagation()">
              <div class="p-6 border-b">
                <div class="flex items-center justify-between">
                  <h2 class="text-2xl font-bold text-gray-900">Detalhes da Solicitação</h2>
                  <button (click)="fecharModal()" class="text-gray-500 hover:text-gray-700">
                    <span class="text-2xl">&times;</span>
                  </button>
                </div>
              </div>

              <div class="p-6">
                <!-- Status e Categoria -->
                <div class="flex items-center gap-3 mb-4">
                  <span class="bg-blue-100 text-blue-800 text-sm font-semibold px-3 py-1 rounded-full">
                    {{ solicitacaoSelecionada.categoriaNome }}
                  </span>
                  <span [class]="getStatusClass(solicitacaoSelecionada.status!)">
                    {{ getStatusText(solicitacaoSelecionada.status!) }}
                  </span>
                </div>

                <!-- Data de Criação -->
                <div class="text-sm text-gray-600 mb-4">
                  📅 Criada em: {{ formatarDataCompleta(solicitacaoSelecionada.dataCriacao) }}
                </div>

                <!-- Localização -->
                <div class="flex items-center text-gray-700 mb-4">
                  <span class="mr-2">📍</span>
                  <span>{{ solicitacaoSelecionada.cidade }}, {{ solicitacaoSelecionada.estado }}</span>
                </div>

                <!-- Descrição -->
                <div class="mb-6">
                  <h3 class="font-semibold text-gray-900 mb-2">Descrição</h3>
                  <p class="text-gray-700">{{ solicitacaoSelecionada.descricao }}</p>
                </div>

                <!-- Propostas -->
                <div class="border-t pt-4 mt-4">
                  <a
                    [routerLink]="['/cliente/solicitacao', solicitacaoSelecionada.id, 'propostas']"
                    (click)="fecharModal()"
                    class="w-full block text-center py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold">
                    Ver Propostas Recebidas
                  </a>
                </div>

                <!-- Ações -->
                @if (podeEditarOuExcluir()) {
                  <div class="border-t pt-4 mt-4">
                    <h3 class="font-semibold text-gray-900 mb-3">Ações</h3>
                    <div class="flex gap-3">
                      <button
                        (click)="editarSolicitacao()"
                        class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium">
                        ✏️ Editar
                      </button>
                      <button
                        (click)="confirmarExclusao()"
                        class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium">
                        🗑️ Cancelar
                      </button>
                    </div>
                  </div>
                }

                <!-- Mensagem de erro -->
                @if (errorMessage) {
                  <div class="mt-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                    {{ errorMessage }}
                  </div>
                }
              </div>
            </div>
          </div>
        }

        <!-- Modal de Confirmação de Exclusão -->
        @if (mostrarModalExclusao) {
          <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" (click)="fecharModalExclusao()">
            <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4" (click)="$event.stopPropagation()">
              <div class="p-6">
                <div class="text-center mb-4">
                  <span class="text-5xl">⚠️</span>
                </div>
                <h2 class="text-xl font-bold text-gray-900 text-center mb-2">Confirmar Cancelamento</h2>
                <p class="text-gray-600 text-center mb-6">
                  Tem certeza que deseja excluir esta solicitação? Esta ação não pode ser desfeita.
                </p>
                <div class="flex gap-3">
                  <button 
                    (click)="fecharModalExclusao()"
                    class="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition font-medium">
                    Cancelar
                  </button>
                  <button 
                    (click)="excluirSolicitacao()"
                    [disabled]="excluindo"
                    class="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium disabled:opacity-50">
                    @if (excluindo) {
                      <span>Cancelando...</span>
                    } @else {
                      <span>Confirmar</span>
                    }
                  </button>
                </div>
              </div>
            </div>
          </div>
        }
      </div>
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
    private authService: AuthService
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
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  verDetalhes(id: number): void {
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (solicitacao) => {
        this.solicitacaoSelecionada = solicitacao;
        this.errorMessage = '';
      },
      error: (error) => {
        console.error('Erro ao carregar detalhes da solicitação', error);
        this.errorMessage = 'Erro ao carregar detalhes da solicitação.';
      }
    });
  }

  fecharModal(): void {
    this.solicitacaoSelecionada = null;
    this.errorMessage = '';
  }

  podeEditarOuExcluir(): boolean {
    if (!this.solicitacaoSelecionada) return false;
    return this.solicitacaoSelecionada.status === 'ABERTA';
  }

  editarSolicitacao(): void {
    if (!this.solicitacaoSelecionada) return;
    this.fecharModal();
  }

  confirmarExclusao(): void {
    this.mostrarModalExclusao = true;
  }

  fecharModalExclusao(): void {
    this.mostrarModalExclusao = false;
  }

  excluirSolicitacao(): void {
    if (!this.solicitacaoSelecionada) return;

    this.excluindo = true;
    this.solicitacaoService.deletar(this.solicitacaoSelecionada.id!).subscribe({
      next: () => {
        this.fecharModal();
        this.fecharModalExclusao();
        this.carregarSolicitacoes();
        this.excluindo = false;
      },
      error: (error) => {
        console.error('Erro ao cancelar solicitação', error);
        this.errorMessage = 'Erro ao cancelar solicitação.';
        this.fecharModalExclusao();
        this.excluindo = false;
      }
    });
  }
}
