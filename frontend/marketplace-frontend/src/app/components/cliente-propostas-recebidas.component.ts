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
  template: `
    <div class="min-h-screen bg-gray-50">
      <!-- Header -->
      <div class="bg-white shadow-sm border-b">
        <div class="container mx-auto px-4 py-6">
          <div class="flex items-center gap-4">
            <button
              (click)="voltar()"
              class="text-gray-500 hover:text-gray-700 transition">
              ← Voltar
            </button>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Propostas Recebidas</h1>
              @if (solicitacao) {
                <p class="text-gray-600 text-sm mt-1">
                  {{ solicitacao.categoriaNome }} · {{ solicitacao.cidade }}, {{ solicitacao.estado }}
                </p>
              }
            </div>
          </div>
        </div>
      </div>

      <div class="container mx-auto px-4 py-8 max-w-3xl">

        <!-- Loading inicial -->
        @if (loading) {
          <div class="text-center py-16">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p class="text-gray-600 mt-4">Carregando propostas...</p>
          </div>
        }

        @else {
          <!-- Card da solicitação -->
          @if (solicitacao) {
            <div class="bg-white rounded-lg shadow-md p-5 mb-6">
              <div class="flex items-start justify-between mb-3">
                <span class="bg-blue-100 text-blue-800 text-sm font-semibold px-3 py-1 rounded-full">
                  {{ solicitacao.categoriaNome }}
                </span>
                <span [class]="getStatusSolicitacaoClass(solicitacao.status!)">
                  {{ getStatusSolicitacaoText(solicitacao.status!) }}
                </span>
              </div>
              <p class="text-gray-700 text-sm leading-relaxed line-clamp-3">{{ solicitacao.descricao }}</p>
            </div>
          }

          <!-- Banner de sucesso (proposta aceita) -->
          @if (mensagemSucesso) {
            <div class="bg-green-50 border-l-4 border-green-500 p-4 rounded-lg mb-6">
              <div class="flex items-center gap-3">
                <span class="text-2xl">✅</span>
                <p class="text-green-800 font-medium">{{ mensagemSucesso }}</p>
              </div>
            </div>
          }

          <!-- Nenhuma proposta -->
          @if (propostas.length === 0) {
            <div class="bg-white rounded-lg shadow-md p-12 text-center">
              <span class="text-6xl">📬</span>
              <p class="text-gray-700 text-lg mt-4 font-medium">Nenhuma proposta recebida ainda</p>
              <p class="text-gray-500 text-sm mt-2">
                Prestadores qualificados poderão ver sua solicitação e enviar propostas em breve.
              </p>
              <button
                (click)="voltar()"
                class="mt-6 px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium">
                Voltar ao Painel
              </button>
            </div>
          }

          <!-- Lista de propostas -->
          @if (propostas.length > 0) {
            <div class="space-y-4">
              @for (proposta of propostas; track proposta.id) {
                <div class="bg-white rounded-lg shadow-md overflow-hidden"
                     [class.ring-2]="proposta.status === StatusProposta.ACEITA"
                     [class.ring-green-400]="proposta.status === StatusProposta.ACEITA">

                  <!-- Cabeçalho do card -->
                  <div class="p-5 border-b">
                    <div class="flex items-center justify-between">
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                          <span class="text-blue-700 font-bold text-lg">
                            {{ proposta.prestadorNome?.charAt(0)?.toUpperCase() }}
                          </span>
                        </div>
                        <div>
                          <p class="font-semibold text-gray-900">{{ proposta.prestadorNome }}</p>
                          <p class="text-xs text-gray-500">{{ formatarData(proposta.dataEnvio) }}</p>
                        </div>
                      </div>
                      <span [class]="getStatusPropostaClass(proposta.status!)">
                        {{ getStatusPropostaText(proposta.status!) }}
                      </span>
                    </div>
                  </div>

                  <!-- Corpo -->
                  <div class="p-5">
                    <!-- Valor em destaque -->
                    <div class="text-3xl font-bold text-green-600 mb-3">
                      {{ formatarValor(proposta.valor) }}
                    </div>

                    <!-- Mensagem -->
                    @if (proposta.mensagem) {
                      <p class="text-gray-700 text-sm leading-relaxed">{{ proposta.mensagem }}</p>
                    }
                  </div>

                  <!-- Ações (apenas para propostas ENVIADAS com solicitação ABERTA) -->
                  @if (proposta.status === StatusProposta.ENVIADA && solicitacao?.status === 'ABERTA') {
                    <div class="px-5 pb-5">
                      <!-- Confirmação inline de aceite -->
                      @if (idConfirmandoAceite === proposta.id) {
                        <div class="bg-green-50 border border-green-200 rounded-lg p-4">
                          <p class="text-green-800 font-medium mb-3">
                            Aceitar a proposta de {{ proposta.prestadorNome }} por {{ formatarValor(proposta.valor) }}?
                          </p>
                          <p class="text-green-700 text-sm mb-4">
                            A solicitação passará para <strong>Em Andamento</strong> e você poderá entrar em contato com o prestador.
                          </p>
                          <div class="flex gap-3">
                            <button
                              (click)="aceitarProposta(proposta.id!)"
                              [disabled]="processando === proposta.id"
                              class="flex-1 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-medium disabled:opacity-50">
                              @if (processando === proposta.id) {
                                Aceitando...
                              } @else {
                                Confirmar Aceite
                              }
                            </button>
                            <button
                              (click)="cancelarConfirmacao()"
                              class="flex-1 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition font-medium text-gray-700">
                              Cancelar
                            </button>
                          </div>
                        </div>
                      } @else {
                        <div class="flex gap-3">
                          <button
                            (click)="confirmarAceite(proposta.id!)"
                            class="flex-1 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition font-semibold">
                            Aceitar Proposta
                          </button>
                          <button
                            (click)="recusarProposta(proposta.id!)"
                            [disabled]="processando === proposta.id"
                            class="px-4 py-2 border border-red-300 text-red-600 rounded-lg hover:bg-red-50 transition font-medium disabled:opacity-50">
                            @if (processando === proposta.id) {
                              ...
                            } @else {
                              Recusar
                            }
                          </button>
                        </div>
                      }

                      <!-- Erro por proposta -->
                      @if (erroProposta === proposta.id) {
                        <p class="text-red-600 text-sm mt-2">{{ mensagemErro }}</p>
                      }
                    </div>
                  }
                </div>
              }
            </div>
          }
        }
      </div>
    </div>
  `
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
