import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PrestadorService } from '../services/prestador.service';
import { AuthService } from '../services/auth.service';
import { SolicitacaoServico, Prestador } from '../models/models';

@Component({
  selector: 'app-prestador-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen bg-gray-50">
      <!-- Header -->
      <div class="bg-white shadow-sm border-b">
        <div class="container mx-auto px-4 py-6">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-3xl font-bold text-gray-900">Painel do Prestador</h1>
              <p class="text-gray-600 mt-1">Bem-vindo, {{ userName }}</p>
            </div>
            <button 
              routerLink="/prestador/perfil"
              class="px-6 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition">
              {{ prestador ? 'Editar Perfil' : 'Completar Perfil' }}
            </button>
          </div>
        </div>
      </div>

      <div class="container mx-auto px-4 py-8">
        <!-- Alert se perfil incompleto -->
        @if (!prestador) {
          <div class="mb-6 bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded-lg">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <span class="text-2xl">⚠️</span>
              </div>
              <div class="ml-3">
                <h3 class="text-lg font-semibold text-yellow-800">Complete seu perfil!</h3>
                <p class="text-yellow-700 mt-1">
                  Você precisa completar seu perfil para começar a receber solicitações de serviço.
                </p>
                <button 
                  routerLink="/prestador/perfil"
                  class="mt-3 px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 transition">
                  Completar Agora
                </button>
              </div>
            </div>
          </div>
        }

        <!-- Stats Cards -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Solicitações Abertas</p>
                <p class="text-3xl font-bold text-blue-600 mt-2">{{ solicitacoes.length }}</p>
              </div>
              <div class="text-4xl">📋</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Propostas Enviadas</p>
                <p class="text-3xl font-bold text-green-600 mt-2">0</p>
              </div>
              <div class="text-4xl">📤</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Avaliação Média</p>
                <p class="text-3xl font-bold text-yellow-600 mt-2">
                  {{ prestador?.avaliacaoMedia || '-' }}
                </p>
              </div>
              <div class="text-4xl">⭐</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Serviços Concluídos</p>
                <p class="text-3xl font-bold text-purple-600 mt-2">0</p>
              </div>
              <div class="text-4xl">✅</div>
            </div>
          </div>
        </div>

        <!-- Perfil Info -->
        @if (prestador) {
          <div class="bg-white rounded-lg shadow-md p-6 mb-8">
            <h2 class="text-xl font-bold text-gray-900 mb-4">Meu Perfil</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p class="text-gray-600 text-sm">Localização</p>
                <p class="font-semibold">{{ prestador.cidade }}, {{ prestador.estado }}</p>
              </div>
              <div>
                <p class="text-gray-600 text-sm">Categorias de Serviço</p>
                <p class="font-semibold">{{ prestador.categoriasIds?.length || 0 }} categoria(s)</p>
              </div>
            </div>
            @if (prestador.descricaoPerfil) {
              <div class="mt-4">
                <p class="text-gray-600 text-sm">Sobre</p>
                <p class="text-gray-800">{{ prestador.descricaoPerfil }}</p>
              </div>
            }
          </div>
        }

        <!-- Solicitações Abertas -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-gray-900">Solicitações Abertas</h2>
            <button 
              (click)="carregarSolicitacoes()"
              class="text-blue-600 hover:text-blue-700 font-medium">
              🔄 Atualizar
            </button>
          </div>

          @if (loading) {
            <div class="text-center py-12">
              <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
              <p class="text-gray-600 mt-4">Carregando solicitações...</p>
            </div>
          } @else if (solicitacoes.length === 0) {
            <div class="text-center py-12">
              <span class="text-6xl">📭</span>
              <p class="text-gray-600 mt-4 text-lg">Nenhuma solicitação disponível no momento</p>
              <p class="text-gray-500 text-sm mt-2">Novas oportunidades aparecerão aqui</p>
            </div>
          } @else {
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              @for (solicitacao of solicitacoes; track solicitacao.id) {
                <div class="border rounded-lg p-5 hover:shadow-lg transition">
                  <div class="flex items-start justify-between mb-3">
                    <span class="bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full">
                      {{ solicitacao.categoriaNome }}
                    </span>
                    <span class="text-xs text-gray-500">
                      {{ formatarData(solicitacao.dataCriacao) }}
                    </span>
                  </div>
                  
                  <p class="text-gray-800 mb-4 line-clamp-3">{{ solicitacao.descricao }}</p>
                  
                  <div class="space-y-2 mb-4 text-sm text-gray-600">
                    <div class="flex items-center">
                      <span class="mr-2">👤</span>
                      <span>{{ solicitacao.clienteNome }}</span>
                    </div>
                    <div class="flex items-center">
                      <span class="mr-2">📍</span>
                      <span>{{ solicitacao.cidade }}, {{ solicitacao.estado }}</span>
                    </div>
                  </div>
                  
                  <button 
                    class="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition font-medium">
                    Ver Detalhes e Enviar Proposta
                  </button>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `
})
export class PrestadorDashboardComponent implements OnInit {
  solicitacoes: SolicitacaoServico[] = [];
  prestador?: Prestador;
  userName: string = '';
  loading = false;

  constructor(
    private solicitacaoService: SolicitacaoService,
    private prestadorService: PrestadorService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userName = this.authService.getCurrentUserName() || 'Prestador';
    this.carregarPerfil();
    this.carregarSolicitacoes();
  }

  carregarPerfil() {
    const userId = this.authService.getCurrentUserId();
    if (!userId) return;

    this.prestadorService.buscarPorUsuario(userId).subscribe({
      next: (prestador: Prestador) => {
        this.prestador = prestador;
      },
      error: (error: any) => {
        console.log('Perfil não encontrado');
      }
    });
  }

  carregarSolicitacoes() {
    this.loading = true;
    this.solicitacaoService.buscarAbertas().subscribe({
      next: (response) => {
        this.solicitacoes = response.content;
        this.loading = false;
      },
      error: (error) => {
        console.error('Erro ao carregar solicitações', error);
        this.loading = false;
      }
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
