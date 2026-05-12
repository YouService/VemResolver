import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { AvaliacaoService } from '../services/avaliacao.service';
import { ViaCEPService } from '../services/viacep.service';
import { MaskDirective } from '../directives/mask.directive';
import { SeletorUfComponent } from './seletor-uf.component';
import { User, UpdatePerfilRequest, AlterarSenhaRequest, Avaliacao } from '../models/models';

@Component({
  selector: 'app-cliente-perfil',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule, MaskDirective, SeletorUfComponent],
  template: `
    <div class="min-h-screen bg-gray-50">
      <div class="container mx-auto px-4 py-8">
        <div class="max-w-4xl mx-auto">
          <div class="mb-6">
            <h1 class="text-3xl font-bold text-gray-900">Meu Perfil</h1>
            <p class="text-gray-600 mt-2">Gerencie suas informações pessoais</p>
          </div>

          @if (loading) {
            <div class="text-center py-12">
              <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
              <p class="text-gray-600 mt-4">Carregando perfil...</p>
            </div>
          } @else {
            <div class="bg-white rounded-lg shadow-md">
              <div class="border-b">
                <div class="flex">
                  <button
                    (click)="abaAtiva = 'dados'"
                    [class.bg-blue-600]="abaAtiva === 'dados'"
                    [class.text-white]="abaAtiva === 'dados'"
                    [class.text-gray-600]="abaAtiva !== 'dados'"
                    [class.hover:bg-gray-50]="abaAtiva !== 'dados'"
                    class="px-6 py-4 font-medium transition">
                    Dados Pessoais
                  </button>
                  <button
                    (click)="abaAtiva = 'senha'"
                    [class.bg-blue-600]="abaAtiva === 'senha'"
                    [class.text-white]="abaAtiva === 'senha'"
                    [class.text-gray-600]="abaAtiva !== 'senha'"
                    [class.hover:bg-gray-50]="abaAtiva !== 'senha'"
                    class="px-6 py-4 font-medium transition">
                    Alterar Senha
                  </button>
                  <button
                    (click)="abaAtiva = 'avaliacoes'; carregarAvaliacoes()"
                    [class.bg-blue-600]="abaAtiva === 'avaliacoes'"
                    [class.text-white]="abaAtiva === 'avaliacoes'"
                    [class.text-gray-600]="abaAtiva !== 'avaliacoes'"
                    [class.hover:bg-gray-50]="abaAtiva !== 'avaliacoes'"
                    class="px-6 py-4 font-medium transition">
                    Minhas Avaliações
                  </button>
                  <button
                    (click)="abaAtiva = 'excluir'"
                    [class.bg-red-600]="abaAtiva === 'excluir'"
                    [class.text-white]="abaAtiva === 'excluir'"
                    [class.text-gray-600]="abaAtiva !== 'excluir'"
                    [class.hover:bg-gray-50]="abaAtiva !== 'excluir'"
                    class="px-6 py-4 font-medium transition">
                    Excluir Conta
                  </button>
                </div>
              </div>

              <div class="p-6">
                @if (abaAtiva === 'dados') {
                  <form [formGroup]="perfilForm" (ngSubmit)="salvarPerfil()">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Nome Completo *</label>
                        <input
                          type="text"
                          formControlName="nome"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Digite seu nome">
                        @if (perfilForm.get('nome')?.invalid && perfilForm.get('nome')?.touched) {
                          <p class="text-red-600 text-sm mt-1">Nome é obrigatório</p>
                        }
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Email *</label>
                        <input
                          type="email"
                          formControlName="email"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="email@exemplo.com">
                        @if (perfilForm.get('email')?.invalid && perfilForm.get('email')?.touched) {
                          <p class="text-red-600 text-sm mt-1">Email inválido</p>
                        }
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Telefone</label>
                        <input
                          type="text"
                          formControlName="telefone"
                          appMask="(00) 00000-0000"
                          maxlength="15"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="(00) 00000-0000">
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">CPF</label>
                        <input
                          type="text"
                          formControlName="cpf"
                          class="w-full px-4 py-2 border rounded-lg bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                          placeholder="000.000.000-00"
                          [attr.disabled]="true"
                          appMask="000.000.000-00"
                          maxlength="14">
                        <p class="text-xs text-gray-500 mt-1">CPF não pode ser alterado</p>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">RG</label>
                        <input
                          type="text"
                          formControlName="rg"
                          class="w-full px-4 py-2 border rounded-lg bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                          placeholder="RG"
                          [attr.disabled]="true"
                        >
                        <p class="text-xs text-gray-500 mt-1">RG não pode ser alterado</p>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Órgão Expedidor</label>
                        <input
                          type="text"
                          formControlName="orgaoExpedidor"
                          class="w-full px-4 py-2 border rounded-lg bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                          placeholder="Ex: SSP/SP"
                          [attr.disabled]="true"
                        >
                        <p class="text-xs text-gray-500 mt-1">Órgão expedidor não pode ser alterado</p>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Data de Expedição</label>
                        <input
                          type="text"
                          formControlName="dataExpedicaoRg"
                          class="w-full px-4 py-2 border rounded-lg bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                          placeholder="DD/MM/AAAA"
                          [attr.disabled]="true"
                        >
                        <p class="text-xs text-gray-500 mt-1">Data de expedição não pode ser alterada</p>
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">CEP</label>
                        <input
                          type="text"
                          formControlName="cep"
                          appMask="00000-000"
                          maxlength="9"
                          (blur)="buscarEnderecoPorCEP()"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="00000-000">
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Cidade</label>
                        <input
                          type="text"
                          formControlName="cidade"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Sua cidade">
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Estado (UF)</label>
                        <app-seletor-uf
                          [uf]="perfilForm.get('estado')?.value || ''"
                          (ufChange)="perfilForm.get('estado')?.setValue($event)">
                        </app-seletor-uf>
                      </div>

                      <div class="md:col-span-2">
                        <label class="block text-sm font-medium text-gray-700 mb-2">Endereço</label>
                        <input
                          type="text"
                          formControlName="endereco"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Rua, número, complemento">
                      </div>
                    </div>

                    <div class="mt-6 flex gap-3">
                      <button
                        type="submit"
                        [disabled]="salvando"
                        class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium disabled:bg-gray-400">
                        {{ salvando ? 'Salvando...' : 'Salvar Alterações' }}
                      </button>
                      <button
                        type="button"
                        (click)="voltarParaDashboard()"
                        [disabled]="salvando"
                        class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition font-medium disabled:bg-gray-100">
                        Cancelar
                      </button>
                    </div>

                    @if (successMessage) {
                      <div class="mt-4 p-4 bg-green-100 text-green-800 rounded-lg">
                        {{ successMessage }}
                      </div>
                    }

                    @if (errorMessage) {
                      <div class="mt-4 p-4 bg-red-100 text-red-800 rounded-lg">
                        {{ errorMessage }}
                      </div>
                    }
                  </form>
                }

                @if (abaAtiva === 'senha') {
                  <form [formGroup]="senhaForm" (ngSubmit)="alterarSenha()">
                    <div class="space-y-6">
                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Senha Atual *</label>
                        <input
                          type="password"
                          formControlName="senhaAtual"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Digite sua senha atual">
                        @if (senhaForm.get('senhaAtual')?.invalid && senhaForm.get('senhaAtual')?.touched) {
                          <p class="text-red-600 text-sm mt-1">Senha atual é obrigatória</p>
                        }
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Nova Senha *</label>
                        <input
                          type="password"
                          formControlName="novaSenha"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Mínimo 6 caracteres">
                        @if (senhaForm.get('novaSenha')?.invalid && senhaForm.get('novaSenha')?.touched) {
                          <p class="text-red-600 text-sm mt-1">Nova senha deve ter no mínimo 6 caracteres</p>
                        }
                      </div>

                      <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">Confirmar Nova Senha *</label>
                        <input
                          type="password"
                          formControlName="confirmarSenha"
                          class="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                          placeholder="Digite a senha novamente">
                        @if (senhaForm.get('confirmarSenha')?.invalid && senhaForm.get('confirmarSenha')?.touched) {
                          <p class="text-red-600 text-sm mt-1">Confirmação de senha é obrigatória</p>
                        }
                      </div>
                    </div>

                    <div class="mt-6 flex gap-3">
                      <button
                        type="submit"
                        [disabled]="alterandoSenha"
                        class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium disabled:bg-gray-400">
                        {{ alterandoSenha ? 'Alterando...' : 'Alterar Senha' }}
                      </button>
                      <button
                        type="button"
                        (click)="voltarParaDashboard()"
                        [disabled]="alterandoSenha"
                        class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition font-medium disabled:bg-gray-100">
                        Cancelar
                      </button>
                    </div>

                    @if (successMessageSenha) {
                      <div class="mt-4 p-4 bg-green-100 text-green-800 rounded-lg">
                        {{ successMessageSenha }}
                      </div>
                    }

                    @if (errorMessageSenha) {
                      <div class="mt-4 p-4 bg-red-100 text-red-800 rounded-lg">
                        {{ errorMessageSenha }}
                      </div>
                    }
                  </form>
                }

                @if (abaAtiva === 'avaliacoes') {
                  <div>
                    @if (loadingAvaliacoes) {
                      <div class="text-center py-8">
                        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                      </div>
                    } @else if (avaliacoes.length === 0) {
                      <div class="text-center py-12">
                        <span class="text-5xl">⭐</span>
                        <p class="text-gray-600 mt-4">Você ainda não recebeu avaliações de prestadores.</p>
                        <p class="text-gray-500 text-sm mt-1">As avaliações aparecem aqui após um prestador avaliar seu perfil como cliente.</p>
                      </div>
                    } @else {
                      <div class="mb-4 p-4 bg-blue-50 rounded-lg flex items-center gap-4">
                        <div class="text-center">
                          <p class="text-3xl font-bold text-blue-700">{{ mediaAvaliacoes | number:'1.1-1' }}</p>
                          <div class="flex text-yellow-500 text-lg justify-center">
                            @for (star of [1,2,3,4,5]; track star) {
                              <span>{{ star <= mediaAvaliacoes ? '★' : '☆' }}</span>
                            }
                          </div>
                          <p class="text-xs text-gray-500 mt-1">{{ avaliacoes.length }} avaliação{{ avaliacoes.length !== 1 ? 'ões' : '' }}</p>
                        </div>
                        <div class="flex-1">
                          <p class="text-sm font-semibold text-gray-800">Sua reputação como cliente</p>
                          <p class="text-sm text-gray-600 mt-1">Esta nota é visível para prestadores ao avaliar se aceitam seus serviços.</p>
                        </div>
                      </div>

                      <div class="space-y-4">
                        @for (av of avaliacoes; track av.id) {
                          <div class="border border-gray-200 rounded-lg p-4">
                            <div class="flex items-start justify-between">
                              <div class="flex items-center gap-3">
                                <div class="w-9 h-9 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                                  {{ (av.nomeAvaliador || '?').charAt(0).toUpperCase() }}
                                </div>
                                <div>
                                  <p class="font-semibold text-gray-900 text-sm">{{ av.nomeAvaliador }}</p>
                                  <p class="text-xs text-gray-500">Prestador</p>
                                </div>
                              </div>
                              <div class="flex text-yellow-500">
                                @for (star of [1,2,3,4,5]; track star) {
                                  <span>{{ star <= (av.nota || 0) ? '★' : '☆' }}</span>
                                }
                              </div>
                            </div>
                            @if (av.comentario) {
                              <p class="text-gray-700 text-sm mt-3 pl-12">{{ av.comentario }}</p>
                            }
                          </div>
                        }
                      </div>
                    }
                  </div>
                }

                @if (abaAtiva === 'excluir') {
                  <div class="space-y-6">
                    <div class="p-4 bg-red-50 border-l-4 border-red-500 rounded-lg">
                      <h3 class="text-lg font-bold text-red-800 mb-2">⚠️ Ação Perigosa</h3>
                      <p class="text-red-700">Esta ação não pode ser desfeita. Ao excluir sua conta:</p>
                      <ul class="list-disc list-inside text-red-700 mt-2 space-y-1">
                        <li>Todos os seus dados pessoais serão removidos</li>
                        <li>Suas solicitações de serviço serão desvinculadas</li>
                        <li>Você não poderá mais acessar sua conta</li>
                      </ul>
                    </div>

                    @if (!confirmarExclusao) {
                      <div class="flex gap-3">
                        <button
                          (click)="confirmarExclusao = true"
                          [disabled]="excluindoConta"
                          class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium disabled:bg-gray-400">
                          Quero Excluir Minha Conta
                        </button>
                        <button
                          (click)="abaAtiva = 'dados'"
                          [disabled]="excluindoConta"
                          class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition font-medium disabled:bg-gray-100">
                          Voltar
                        </button>
                      </div>
                    } @else {
                      <div class="p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                        <p class="text-yellow-800 font-medium mb-4">Tem certeza absoluta que deseja excluir sua conta?</p>
                        <div class="flex gap-3">
                          <button
                            (click)="deletarConta()"
                            [disabled]="excluindoConta"
                            class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium disabled:bg-gray-400">
                            {{ excluindoConta ? 'Excluindo...' : 'Sim, Excluir Definitivamente' }}
                          </button>
                          <button
                            (click)="confirmarExclusao = false"
                            [disabled]="excluindoConta"
                            class="px-6 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition font-medium disabled:bg-gray-100">
                            Cancelar
                          </button>
                        </div>
                      </div>
                    }

                    @if (errorMessageExclusao) {
                      <div class="p-4 bg-red-100 text-red-800 rounded-lg">
                        {{ errorMessageExclusao }}
                      </div>
                    }
                  </div>
                }
              </div>
            </div>
          }
        </div>
      </div>
    </div>
  `
})
export class ClientePerfilComponent implements OnInit {
  abaAtiva: 'dados' | 'senha' | 'avaliacoes' | 'excluir' = 'dados';
  loading = false;
  salvando = false;
  alterandoSenha = false;
  excluindoConta = false;
  successMessage = '';
  errorMessage = '';
  successMessageSenha = '';
  errorMessageSenha = '';
  errorMessageExclusao = '';
  confirmarExclusao = false;
  avaliacoes: Avaliacao[] = [];
  loadingAvaliacoes = false;
  avaliacoesCarregadas = false;

  get mediaAvaliacoes(): number {
    if (!this.avaliacoes.length) return 0;
    return this.avaliacoes.reduce((sum, a) => sum + (a.nota || 0), 0) / this.avaliacoes.length;
  }

  perfilForm: FormGroup;
  senhaForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private avaliacaoService: AvaliacaoService,
    private viaCEPService: ViaCEPService,
    private router: Router
  ) {
    this.perfilForm = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      telefone: [''],
      cpf: [''],
      rg: [''],
      orgaoExpedidor: [''],
      dataExpedicaoRg: [''],
      cep: [''],
      endereco: [''],
      cidade: [''],
      estado: ['']
    });

    this.senhaForm = this.fb.group({
      senhaAtual: ['', [Validators.required]],
      novaSenha: ['', [Validators.required, Validators.minLength(6)]],
      confirmarSenha: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.carregarPerfil();
    this.setupDataExpedicaoMask();
  }

  setupDataExpedicaoMask(): void {
    this.perfilForm.get('dataExpedicaoRg')?.valueChanges.subscribe(value => {
      if (!value) return;

      const v = value.replace(/\D/g, '');

      if (v.length > 8) {
        this.perfilForm.get('dataExpedicaoRg')?.setValue(v.substring(0, 8), { emitEvent: false });
        return;
      }

      let formatted = '';
      for (let i = 0; i < v.length; i++) {
        if (i === 2 || i === 4) {
          formatted += '/';
        }
        formatted += v[i];
      }

      this.perfilForm.get('dataExpedicaoRg')?.setValue(formatted, { emitEvent: false });
    });
  }

  carregarPerfil() {
    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.buscarPerfil().subscribe({
      next: (user) => {
        this.perfilForm.patchValue({
          nome: user.nome,
          email: user.email,
          telefone: user.telefone || '',
          cpf: user.cpf || '',
          rg: user.rg || '',
          orgaoExpedidor: user.orgaoExpedidor || '',
          dataExpedicaoRg: user.dataExpedicaoRg || '',
          cep: user.cep || '',
          endereco: user.endereco || '',
          cidade: user.cidade || '',
          estado: user.estado || ''
        });
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar perfil', error);
        this.errorMessage = 'Erro ao carregar perfil';
        this.loading = false;
      }
    });
  }

  salvarPerfil() {
    if (this.perfilForm.invalid) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    this.salvando = true;
    this.successMessage = '';
    this.errorMessage = '';

    const dados: UpdatePerfilRequest = this.perfilForm.value;

    this.authService.atualizarPerfil(dados).subscribe({
      next: () => {
        this.successMessage = 'Perfil atualizado com sucesso!';
        this.salvando = false;
        setTimeout(() => {
          this.successMessage = '';
          this.router.navigate(['/cliente/dashboard']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('Erro ao atualizar perfil', error);
        this.errorMessage = error.error?.message || 'Erro ao atualizar perfil';
        this.salvando = false;
      }
    });
  }

  alterarSenha() {
    if (this.senhaForm.invalid) {
      this.senhaForm.markAllAsTouched();
      return;
    }

    const senha = this.senhaForm.value;
    if (senha.novaSenha !== senha.confirmarSenha) {
      this.errorMessageSenha = 'Nova senha e confirmação não conferem';
      return;
    }

    this.alterandoSenha = true;
    this.successMessageSenha = '';
    this.errorMessageSenha = '';

    const dados: AlterarSenhaRequest = this.senhaForm.value;

    this.authService.alterarSenha(dados).subscribe({
      next: () => {
        this.successMessageSenha = 'Senha alterada com sucesso!';
        this.senhaForm.reset();
        this.alterandoSenha = false;
        setTimeout(() => {
          this.successMessageSenha = '';
          this.router.navigate(['/cliente/dashboard']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('Erro ao alterar senha', error);
        this.errorMessageSenha = error.error?.message || 'Erro ao alterar senha';
        this.alterandoSenha = false;
      }
    });
  }

  voltarParaDashboard() {
    this.router.navigate(['/cliente/dashboard']);
  }

  buscarEnderecoPorCEP() {
    const cep = this.perfilForm.get('cep')?.value;
    if (!cep || cep.replace(/\D/g, '').length !== 8) {
      return;
    }

    this.viaCEPService.buscarCEP(cep).subscribe({
      next: (endereco) => {
        if (endereco.erro) {
          console.error('CEP não encontrado');
          return;
        }
        this.perfilForm.patchValue({
          endereco: endereco.logradouro || '',
          cidade: endereco.localidade || '',
          estado: endereco.uf || ''
        });
      },
      error: (error) => {
        console.error('Erro ao buscar CEP', error);
      }
    });
  }

  carregarAvaliacoes() {
    if (this.avaliacoesCarregadas) return;
    const userId = this.authService.getCurrentUserId();
    if (!userId) return;
    this.loadingAvaliacoes = true;
    this.avaliacaoService.listarPorCliente(userId, 0, 20).subscribe({
      next: (response) => {
        this.avaliacoes = response.content;
        this.avaliacoesCarregadas = true;
        this.loadingAvaliacoes = false;
      },
      error: () => { this.loadingAvaliacoes = false; }
    });
  }

  deletarConta() {
    this.excluindoConta = true;
    this.errorMessageExclusao = '';

    this.authService.deletarConta().subscribe({
      next: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('userTipo');
        this.router.navigate(['/login']);
      },
      error: (error: any) => {
        console.error('Erro ao excluir conta', error);
        this.errorMessageExclusao = error.error?.message || 'Erro ao excluir conta';
        this.excluindoConta = false;
        this.confirmarExclusao = false;
      }
    });
  }
}
