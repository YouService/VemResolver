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

        <!-- Alert perfil incompleto -->
        @if (!prestador) {
          <div class="mb-6 bg-yellow-50 border-l-4 border-yellow-400 p-4 rounded-lg">
            <div class="flex items-center">
              <span class="text-2xl mr-3">⚠️</span>
              <div>
                <h3 class="text-lg font-semibold text-yellow-800">Complete seu perfil!</h3>
                <p class="text-yellow-700 mt-1">Você precisa completar seu perfil para começar a receber solicitações.</p>
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
                <p class="text-gray-600 text-sm">Oportunidades Abertas</p>
                <p class="text-3xl font-bold text-blue-600 mt-2">{{ solicitacoes.length }}</p>
              </div>
              <div class="text-4xl">📋</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Propostas Aguardando</p>
                <p class="text-3xl font-bold text-green-600 mt-2">{{ propostasAguardando.length }}</p>
              </div>
              <div class="text-4xl">📤</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Em Andamento</p>
                <p class="text-3xl font-bold text-yellow-600 mt-2">{{ contratosAndamento.length }}</p>
              </div>
              <div class="text-4xl">🔧</div>
            </div>
          </div>

          <div class="bg-white rounded-lg shadow-md p-6">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-gray-600 text-sm">Serviços Concluídos</p>
                <p class="text-3xl font-bold text-purple-600 mt-2">{{ servicosConcluidos.length }}</p>
              </div>
              <div class="text-4xl">✅</div>
            </div>
          </div>
        </div>

        <!-- Contratos em Andamento -->
        @if (contratosAndamento.length > 0) {
          <div class="bg-white rounded-lg shadow-md p-6 mb-8">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-xl font-bold text-gray-900 flex items-center gap-3">
                Contratos em Andamento
                <span class="bg-yellow-100 text-yellow-800 text-sm font-semibold px-2.5 py-0.5 rounded-full">
                  {{ contratosAndamento.length }}
                </span>
              </h2>
              <button (click)="carregarPropostas()" class="text-blue-600 hover:text-blue-700 font-medium">
                🔄 Atualizar
              </button>
            </div>
            <div class="space-y-4">
              @for (proposta of contratosAndamento; track proposta.id) {
                <div class="border-2 border-yellow-300 bg-yellow-50 rounded-lg p-5">
                  <div class="flex items-start justify-between mb-3">
                    <div class="flex items-center gap-3">
                      <span class="bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full">
                        {{ proposta.categoriaNome }}
                      </span>
                      <span class="bg-yellow-200 text-yellow-900 text-xs font-semibold px-3 py-1 rounded-full">
                        Em Andamento
                      </span>
                    </div>
                    <span class="text-sm font-bold text-green-700">
                      R$ {{ proposta.valor | number:'1.2-2' }}
                    </span>
                  </div>

                  <p class="text-gray-800 font-medium mb-3">{{ proposta.solicitacaoDescricao }}</p>

                  <div class="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm text-gray-600">
                    <div class="flex items-center gap-2">
                      <span>👤</span>
                      <span>{{ proposta.clienteNome }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <span>📍</span>
                      <span>{{ proposta.cidade }}, {{ proposta.estado }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <span>📅</span>
                      <span>Aceito {{ formatarData(proposta.dataEnvio) }}</span>
                    </div>
                  </div>

                  <div class="mt-3 pt-3 border-t border-yellow-200">
                    <p class="text-xs text-gray-500">
                      O cliente irá marcar o serviço como concluído quando finalizado.
                    </p>
                  </div>
                </div>
              }
            </div>
          </div>
        }

        <!-- Propostas Aguardando Resposta -->
        @if (propostasAguardando.length > 0) {
          <div class="bg-white rounded-lg shadow-md p-6 mb-8">
            <h2 class="text-xl font-bold text-gray-900 mb-6 flex items-center gap-3">
              Propostas Aguardando Resposta
              <span class="bg-blue-100 text-blue-800 text-sm font-semibold px-2.5 py-0.5 rounded-full">
                {{ propostasAguardando.length }}
              </span>
            </h2>
            <div class="space-y-3">
              @for (proposta of propostasAguardando; track proposta.id) {
                <div class="border border-gray-200 rounded-lg p-4 flex items-center justify-between">
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <span class="bg-blue-100 text-blue-800 text-xs font-semibold px-2 py-0.5 rounded-full">
                        {{ proposta.categoriaNome }}
                      </span>
                    </div>
                    <p class="text-gray-800 text-sm line-clamp-1">{{ proposta.solicitacaoDescricao }}</p>
                    <p class="text-gray-500 text-xs mt-1">📍 {{ proposta.cidade }}, {{ proposta.estado }}</p>
                  </div>
                  <div class="text-right ml-4">
                    <p class="font-bold text-green-700">R$ {{ proposta.valor | number:'1.2-2' }}</p>
                    <p class="text-xs text-gray-500">{{ formatarData(proposta.dataEnvio) }}</p>
                    <span class="inline-block mt-1 bg-blue-100 text-blue-700 text-xs px-2 py-0.5 rounded-full">
                      Aguardando
                    </span>
                  </div>
                </div>
              }
            </div>
          </div>
        }

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
            <h2 class="text-xl font-bold text-gray-900">Novas Oportunidades</h2>
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
                    <div class="flex items-center gap-2">
                      <span>👤</span>
                      <span>{{ solicitacao.clienteNome }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <span>📍</span>
                      <span>{{ solicitacao.cidade }}, {{ solicitacao.estado }}</span>
                    </div>
                  </div>

                  <a
                    [routerLink]="['/prestador/solicitacao', solicitacao.id]"
                    class="w-full block text-center bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition font-medium">
                    Ver Detalhes e Enviar Proposta
                  </a>
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
  minhasPropostas: Proposta[] = [];
  prestador?: Prestador;
  userName = '';
  loading = false;

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

  carregarSolicitacoes() {
    this.loading = true;
    this.solicitacaoService.buscarAbertas(undefined, undefined, undefined, 0, 30).subscribe({
      next: (response) => {
        this.solicitacoes = response.content;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
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
