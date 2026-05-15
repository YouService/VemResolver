import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { CategoriaService } from '../services/categoria.service';
import { Categoria, SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-cliente-nova-solicitacao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-8">
      <div class="container mx-auto px-4 max-w-3xl">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900">Nova Solicitação de Serviço</h1>
          <p class="text-gray-600 mt-2">Descreva o serviço que você precisa e aguarde propostas dos prestadores</p>
        </div>

        <!-- Form Card -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <form [formGroup]="solicitacaoForm" (ngSubmit)="enviarSolicitacao()">
            
            <!-- Categoria -->
            <div class="mb-6">
              <label class="block text-gray-700 font-semibold mb-2">
                Categoria do Serviço
                <span class="text-red-500">*</span>
              </label>
              <select
                formControlName="categoriaId"
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                [class.border-red-500]="solicitacaoForm.get('categoriaId')?.invalid && solicitacaoForm.get('categoriaId')?.touched">
                <option value="">Selecione a categoria...</option>
                @for (categoria of categorias; track categoria.id) {
                  <option [value]="categoria.id">{{ categoria.nome }}</option>
                }
              </select>
              @if (solicitacaoForm.get('categoriaId')?.invalid && solicitacaoForm.get('categoriaId')?.touched) {
                <p class="text-red-500 text-sm mt-1">Selecione uma categoria</p>
              }
            </div>

            <!-- Descrição -->
            <div class="mb-6">
              <label class="block text-gray-700 font-semibold mb-2">
                Descrição do Serviço
                <span class="text-red-500">*</span>
              </label>
              <textarea
                formControlName="descricao"
                rows="6"
                placeholder="Descreva em detalhes o serviço que você precisa, incluindo:
- O que precisa ser feito
- Prazo desejado
- Detalhes importantes
- Materiais necessários (se aplicável)"
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                [class.border-red-500]="solicitacaoForm.get('descricao')?.invalid && solicitacaoForm.get('descricao')?.touched">
              </textarea>
              <div class="flex justify-between items-center mt-1">
                @if (solicitacaoForm.get('descricao')?.invalid && solicitacaoForm.get('descricao')?.touched) {
                  <p class="text-red-500 text-sm">Mínimo de 20 caracteres</p>
                } @else {
                  <p class="text-gray-500 text-sm">Seja o mais específico possível</p>
                }
                <p class="text-gray-500 text-sm">
                  {{ solicitacaoForm.get('descricao')?.value?.length || 0 }} caracteres
                </p>
              </div>
            </div>

            <!-- Localização -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              <div>
                <label class="block text-gray-700 font-semibold mb-2">
                  Cidade
                  <span class="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  formControlName="cidade"
                  placeholder="Ex: São Paulo"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  [class.border-red-500]="solicitacaoForm.get('cidade')?.invalid && solicitacaoForm.get('cidade')?.touched">
                @if (solicitacaoForm.get('cidade')?.invalid && solicitacaoForm.get('cidade')?.touched) {
                  <p class="text-red-500 text-sm mt-1">Cidade é obrigatória</p>
                }
              </div>

              <div>
                <label class="block text-gray-700 font-semibold mb-2">
                  Estado
                  <span class="text-red-500">*</span>
                </label>
                <select
                  formControlName="estado"
                  class="w-24 px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                  [class.border-red-500]="solicitacaoForm.get('estado')?.invalid && solicitacaoForm.get('estado')?.touched">
                  <option value="">UF</option>
                  <option value="AC">AC</option>
                  <option value="AL">AL</option>
                  <option value="AP">AP</option>
                  <option value="AM">AM</option>
                  <option value="BA">BA</option>
                  <option value="CE">CE</option>
                  <option value="DF">DF</option>
                  <option value="ES">ES</option>
                  <option value="GO">GO</option>
                  <option value="MA">MA</option>
                  <option value="MT">MT</option>
                  <option value="MS">MS</option>
                  <option value="MG">MG</option>
                  <option value="PA">PA</option>
                  <option value="PB">PB</option>
                  <option value="PR">PR</option>
                  <option value="PE">PE</option>
                  <option value="PI">PI</option>
                  <option value="RJ">RJ</option>
                  <option value="RN">RN</option>
                  <option value="RS">RS</option>
                  <option value="RO">RO</option>
                  <option value="RR">RR</option>
                  <option value="SC">SC</option>
                  <option value="SP">SP</option>
                  <option value="SE">SE</option>
                  <option value="TO">TO</option>
                </select>
                @if (solicitacaoForm.get('estado')?.invalid && solicitacaoForm.get('estado')?.touched) {
                  <p class="text-red-500 text-sm mt-1">Estado é obrigatório</p>
                }
              </div>
            </div>

            <!-- Info Box -->
            <div class="bg-blue-50 border-l-4 border-blue-400 p-4 mb-6">
              <div class="flex">
                <div class="flex-shrink-0">
                  <span class="text-2xl">💡</span>
                </div>
                <div class="ml-3">
                  <p class="text-sm text-blue-700">
                    <strong>Dica:</strong> Quanto mais detalhes você fornecer, melhores serão as propostas que receberá. 
                    Inclua fotos, medidas, prazos e qualquer informação relevante.
                  </p>
                </div>
              </div>
            </div>

            <!-- Error Message -->
            @if (errorMessage) {
              <div class="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                {{ errorMessage }}
              </div>
            }

            <!-- Success Message -->
            @if (successMessage) {
              <div class="mb-4 p-4 bg-green-100 border border-green-400 text-green-700 rounded-lg">
                {{ successMessage }}
              </div>
            }

            <!-- Buttons -->
            <div class="flex justify-between items-center pt-6 border-t">
              <button
                type="button"
                (click)="voltar()"
                class="px-6 py-3 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition">
                Cancelar
              </button>
              
              <button
                type="submit"
                [disabled]="loading"
                class="px-8 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
                @if (loading) {
                  <span>Publicando...</span>
                } @else {
                  <span>Publicar Solicitação</span>
                }
              </button>
            </div>
          </form>
        </div>

        <!-- O que acontece depois -->
        <div class="mt-8 bg-white rounded-lg shadow-md p-6">
          <h3 class="font-semibold text-gray-900 mb-4">O que acontece depois?</h3>
          <ol class="space-y-3 text-gray-700">
            <li class="flex items-start">
              <span class="font-bold text-blue-600 mr-3">1.</span>
              <span>Prestadores qualificados verão sua solicitação</span>
            </li>
            <li class="flex items-start">
              <span class="font-bold text-blue-600 mr-3">2.</span>
              <span>Você receberá propostas com valores e prazos</span>
            </li>
            <li class="flex items-start">
              <span class="font-bold text-blue-600 mr-3">3.</span>
              <span>Compare as propostas e escolha a melhor</span>
            </li>
            <li class="flex items-start">
              <span class="font-bold text-blue-600 mr-3">4.</span>
              <span>Entre em contato e combine os detalhes do serviço</span>
            </li>
          </ol>
        </div>
      </div>
    </div>
  `
})
export class ClienteNovaSolicitacaoComponent implements OnInit {
  solicitacaoForm: FormGroup;
  categorias: Categoria[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private solicitacaoService: SolicitacaoService,
    private categoriaService: CategoriaService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.solicitacaoForm = this.fb.group({
      categoriaId: ['', Validators.required],
      descricao: ['', [Validators.required, Validators.minLength(20)]],
      cidade: ['', Validators.required],
      estado: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
  }

  carregarCategorias() {
    const categoriaIdParam = this.route.snapshot.queryParamMap.get('categoriaId');
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => {
        this.categorias = response.content;
        if (categoriaIdParam) {
          this.solicitacaoForm.patchValue({ categoriaId: categoriaIdParam });
        }
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar categorias';
      }
    });
  }

  enviarSolicitacao() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.solicitacaoForm.invalid) {
      Object.keys(this.solicitacaoForm.controls).forEach(key => {
        this.solicitacaoForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Por favor, preencha todos os campos obrigatórios';
      return;
    }

    this.loading = true;

    const solicitacao: SolicitacaoServico = {
      categoriaId: parseInt(this.solicitacaoForm.value.categoriaId),
      descricao: this.solicitacaoForm.value.descricao,
      cidade: this.solicitacaoForm.value.cidade,
      estado: this.solicitacaoForm.value.estado
    };

    this.solicitacaoService.criar(solicitacao).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Solicitação publicada com sucesso! Redirecionando...';
        setTimeout(() => {
          this.router.navigate(['/cliente/dashboard']);
        }, 2000);
      },
      error: (error: any) => {
        this.loading = false;
        console.error('Erro ao criar solicitação:', error);
        this.errorMessage = 'Erro ao publicar solicitação. Tente novamente.';
      }
    });
  }

  voltar() {
    this.router.navigate(['/cliente/dashboard']);
  }
}
