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
              <h1 class="text-3xl font-bold text-gray-900">Dashboard do Cliente</h1>
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
                    
                    <button 
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
    </div>
  `
})
export class ClienteDashboardComponent implements OnInit {
  solicitacoes: SolicitacaoServico[] = [];
  solicitacoesFiltradas: SolicitacaoServico[] = [];
  userName: string = '';
  loading = false;
  filtroStatus: string = '';

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
}
