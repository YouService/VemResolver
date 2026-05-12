import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AvaliacaoService } from '../services/avaliacao.service';
import { SolicitacaoService } from '../services/solicitacao.service';
import { SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-prestador-avaliacao',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gray-50 flex items-start justify-center py-12 px-4">
      <div class="w-full max-w-xl">

        <!-- Loading -->
        @if (loading) {
          <div class="bg-white rounded-lg shadow-md p-12 text-center">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
            <p class="text-gray-600">Carregando dados do serviço...</p>
          </div>
        }

        <!-- Erro -->
        @if (!loading && erroCarregamento) {
          <div class="bg-white rounded-lg shadow-md p-8 text-center">
            <span class="text-6xl">⚠️</span>
            <h2 class="text-xl font-bold text-gray-900 mt-4">{{ erroCarregamento }}</h2>
            <button
              routerLink="/prestador/dashboard"
              class="mt-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold">
              Voltar ao Painel
            </button>
          </div>
        }

        <!-- Sucesso -->
        @if (!loading && enviado) {
          <div class="bg-white rounded-lg shadow-md p-8 text-center">
            <span class="text-6xl">🎉</span>
            <h2 class="text-2xl font-bold text-gray-900 mt-4">Avaliação enviada!</h2>
            <p class="text-gray-600 mt-2">
              Obrigado pelo feedback. Sua avaliação ajuda clientes a manterem um bom histórico.
            </p>
            <div class="flex justify-center mt-2">
              @for (star of [1,2,3,4,5]; track star) {
                <span class="text-3xl">{{ star <= notaEnviada ? '★' : '☆' }}</span>
              }
            </div>
            <button
              routerLink="/prestador/dashboard"
              class="mt-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold">
              Voltar ao Painel
            </button>
          </div>
        }

        <!-- Formulário -->
        @if (!loading && !enviado && !erroCarregamento && solicitacao) {
          <div class="bg-white rounded-lg shadow-md overflow-hidden">

            <!-- Header -->
            <div class="bg-blue-600 px-6 py-5">
              <h1 class="text-2xl font-bold text-white">Avaliar Cliente</h1>
              <p class="text-blue-100 mt-1 text-sm">Serviço concluído — compartilhe sua experiência</p>
            </div>

            <!-- Dados do serviço -->
            <div class="px-6 pt-6 pb-4 border-b border-gray-100 bg-gray-50">
              <div class="flex items-start gap-3">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="bg-blue-100 text-blue-800 text-xs font-semibold px-2.5 py-0.5 rounded-full">
                      {{ solicitacao.categoriaNome }}
                    </span>
                    <span class="bg-purple-100 text-purple-800 text-xs font-semibold px-2.5 py-0.5 rounded-full">
                      Concluído
                    </span>
                  </div>
                  <p class="text-gray-800 text-sm">{{ solicitacao.descricao }}</p>
                  <p class="text-gray-500 text-xs mt-1">📍 {{ solicitacao.cidade }}, {{ solicitacao.estado }}</p>
                </div>
              </div>
              @if (solicitacao.clienteNome) {
                <div class="mt-3 flex items-center gap-2">
                  <div class="w-9 h-9 rounded-full bg-green-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                    {{ solicitacao.clienteNome.charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <p class="text-xs text-gray-500">Cliente</p>
                    <p class="font-semibold text-gray-900 text-sm">{{ solicitacao.clienteNome }}</p>
                  </div>
                </div>
              }
            </div>

            <!-- Formulário -->
            <div class="px-6 py-6">

              <!-- Nota com estrelas -->
              <div class="mb-6">
                <label class="block text-sm font-semibold text-gray-700 mb-3">
                  Qual nota você dá para este cliente? <span class="text-red-500">*</span>
                </label>
                <div class="flex gap-2">
                  @for (star of [1,2,3,4,5]; track star) {
                    <button
                      type="button"
                      (click)="nota = star"
                      (mouseenter)="notaHover = star"
                      (mouseleave)="notaHover = 0"
                      class="text-4xl transition-transform hover:scale-110 focus:outline-none"
                      [attr.aria-label]="star + ' estrela' + (star > 1 ? 's' : '')">
                      {{ (notaHover || nota) >= star ? '★' : '☆' }}
                    </button>
                  }
                </div>
                @if (nota > 0) {
                  <p class="text-sm mt-2 font-medium" [class]="notaClass">{{ notaTexto }}</p>
                }
                @if (tentouEnviar && nota === 0) {
                  <p class="text-red-500 text-sm mt-1">Selecione uma nota para continuar.</p>
                }
              </div>

              <!-- Comentário -->
              <div class="mb-6">
                <label class="block text-sm font-semibold text-gray-700 mb-2">
                  Comentário <span class="text-gray-400 font-normal">(opcional)</span>
                </label>
                <textarea
                  [(ngModel)]="comentario"
                  placeholder="Como foi a experiência com este cliente?"
                  rows="4"
                  maxlength="500"
                  class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none text-sm">
                </textarea>
                <p class="text-xs text-gray-400 text-right mt-1">{{ comentario.length }}/500</p>
              </div>

              <!-- Erro de envio -->
              @if (erroEnvio) {
                <div class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                  <p class="text-red-700 text-sm">{{ erroEnvio }}</p>
                </div>
              }

              <!-- Botões -->
              <div class="flex gap-3">
                <button
                  routerLink="/prestador/dashboard"
                  class="flex-1 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-semibold">
                  Cancelar
                </button>
                <button
                  type="button"
                  (click)="enviar()"
                  [disabled]="enviando"
                  class="flex-1 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-semibold disabled:opacity-50 disabled:cursor-not-allowed">
                  {{ enviando ? 'Enviando...' : 'Enviar Avaliação' }}
                </button>
              </div>
            </div>
          </div>
        }

      </div>
    </div>
  `
})
export class PrestadorAvaliacaoComponent implements OnInit {
  solicitacao: SolicitacaoServico | null = null;

  nota = 0;
  notaHover = 0;
  comentario = '';

  loading = true;
  enviando = false;
  enviado = false;
  notaEnviada = 0;
  tentouEnviar = false;
  erroCarregamento = '';
  erroEnvio = '';

  constructor(
    private route: ActivatedRoute,
    private solicitacaoService: SolicitacaoService,
    private avaliacaoService: AvaliacaoService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (sol) => {
        if (sol.status !== 'CONCLUIDA') {
          this.erroCarregamento = 'Este serviço ainda não foi concluído e não pode ser avaliado.';
          this.loading = false;
          return;
        }
        this.solicitacao = sol;
        this.loading = false;
      },
      error: () => {
        this.erroCarregamento = 'Solicitação não encontrada.';
        this.loading = false;
      }
    });
  }

  get notaTexto(): string {
    const textos = ['', 'Péssimo', 'Ruim', 'Regular', 'Bom', 'Excelente'];
    return textos[this.nota] || '';
  }

  get notaClass(): string {
    const classes = ['', 'text-red-600', 'text-orange-500', 'text-yellow-600', 'text-blue-600', 'text-green-600'];
    return classes[this.nota] || '';
  }

  enviar(): void {
    this.tentouEnviar = true;
    this.erroEnvio = '';

    if (this.nota === 0) return;
    if (!this.solicitacao?.id) return;

    this.enviando = true;
    this.avaliacaoService.criarParaCliente({
      solicitacaoId: this.solicitacao.id,
      nota: this.nota,
      comentario: this.comentario || undefined
    } as any).subscribe({
      next: () => {
        this.notaEnviada = this.nota;
        this.enviado = true;
        this.enviando = false;
      },
      error: (err) => {
        this.enviando = false;
        const msg = err?.error?.message || err?.error?.erro;
        if (msg?.includes('já avaliou')) {
          this.erroEnvio = 'Você já avaliou o cliente desta solicitação.';
        } else if (msg?.includes('não prestou')) {
          this.erroEnvio = 'Você não prestou este serviço.';
        } else {
          this.erroEnvio = 'Erro ao enviar avaliação. Tente novamente.';
        }
      }
    });
  }
}
