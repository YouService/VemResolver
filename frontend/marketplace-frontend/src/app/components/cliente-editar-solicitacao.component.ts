import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { SolicitacaoService } from '../services/solicitacao.service';
import { CategoriaService } from '../services/categoria.service';
import { Categoria, SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-cliente-editar-solicitacao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-8">
      <div class="container mx-auto px-4 max-w-3xl">
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900">Editar Solicitação de Serviço</h1>
          <p class="text-gray-600 mt-2">Faça melhorias na sua solicitação</p>
        </div>

        @if (loading) {
          <div class="text-center py-12">
            <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            <p class="text-gray-600 mt-4">Carregando solicitação...</p>
          </div>
        } @else {
          <div class="bg-white rounded-lg shadow-md p-6">
            <form [formGroup]="solicitacaoForm" (ngSubmit)="atualizarSolicitacao()">

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

              <div class="mb-6">
                <label class="block text-gray-700 font-semibold mb-2">
                  Descrição do Serviço
                  <span class="text-red-500">*</span>
                </label>
                <textarea
                  formControlName="descricao"
                  rows="6"
                  placeholder="Descreva em detalhes o serviço que você precisa..."
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
                    class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    [class.border-red-500]="solicitacaoForm.get('estado')?.invalid && solicitacaoForm.get('estado')?.touched">
                    <option value="">Selecione...</option>
                    <option value="AC">Acre</option>
                    <option value="AL">Alagoas</option>
                    <option value="AP">Amapá</option>
                    <option value="AM">Amazonas</option>
                    <option value="BA">Bahia</option>
                    <option value="CE">Ceará</option>
                    <option value="DF">Distrito Federal</option>
                    <option value="ES">Espírito Santo</option>
                    <option value="GO">Goiás</option>
                    <option value="MA">Maranhão</option>
                    <option value="MT">Mato Grosso</option>
                    <option value="MS">Mato Grosso do Sul</option>
                    <option value="MG">Minas Gerais</option>
                    <option value="PA">Pará</option>
                    <option value="PB">Paraíba</option>
                    <option value="PR">Paraná</option>
                    <option value="PE">Pernambuco</option>
                    <option value="PI">Piauí</option>
                    <option value="RJ">Rio de Janeiro</option>
                    <option value="RN">Rio Grande do Norte</option>
                    <option value="RS">Rio Grande do Sul</option>
                    <option value="RO">Rondônia</option>
                    <option value="RR">Roraima</option>
                    <option value="SC">Santa Catarina</option>
                    <option value="SP">São Paulo</option>
                    <option value="SE">Sergipe</option>
                    <option value="TO">Tocantins</option>
                  </select>
                  @if (solicitacaoForm.get('estado')?.invalid && solicitacaoForm.get('estado')?.touched) {
                    <p class="text-red-500 text-sm mt-1">Estado é obrigatório</p>
                  }
                </div>
              </div>

              <div class="bg-blue-50 border-l-4 border-blue-400 p-4 mb-6">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <span class="text-2xl">💡</span>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-blue-700">
                      <strong>Dica:</strong> Melhore sua descrição adicionando mais detalhes sobre o serviço, prazos específicos e materiais necessários. Isso ajudará os prestadores a fazerem propostas mais precisas.
                    </p>
                  </div>
                </div>
              </div>

              @if (errorMessage) {
                <div class="mb-4 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                  {{ errorMessage }}
                </div>
              }

              @if (successMessage) {
                <div class="mb-4 p-4 bg-green-100 border border-green-400 text-green-700 rounded-lg">
                  {{ successMessage }}
                </div>
              }

              <div class="flex justify-between items-center pt-6 border-t">
                <button
                  type="button"
                  (click)="voltar()"
                  class="px-6 py-3 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition">
                  Cancelar
                </button>

                <button
                  type="submit"
                  [disabled]="salvando"
                  class="px-8 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
                  @if (salvando) {
                    <span>Salvando...</span>
                  } @else {
                    <span>Salvar Alterações</span>
                  }
                </button>
              </div>
            </form>
          </div>
        }
      </div>
    </div>
  `
})
export class ClienteEditarSolicitacaoComponent implements OnInit {
  solicitacaoForm: FormGroup;
  categorias: Categoria[] = [];
  loading = true;
  salvando = false;
  errorMessage = '';
  successMessage = '';
  solicitacaoId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private solicitacaoService: SolicitacaoService,
    private categoriaService: CategoriaService,
    private router: Router,
    private route: ActivatedRoute
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
    this.solicitacaoId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.solicitacaoId) {
      this.carregarSolicitacao(this.solicitacaoId);
    }
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => {
        this.categorias = response.content;
      },
      error: (error: any) => {
        console.error('Erro ao carregar categorias:', error);
        this.errorMessage = 'Erro ao carregar categorias';
      }
    });
  }

  carregarSolicitacao(id: number) {
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (solicitacao) => {
        this.solicitacaoForm.patchValue({
          categoriaId: solicitacao.categoriaId,
          descricao: solicitacao.descricao,
          cidade: solicitacao.cidade,
          estado: solicitacao.estado
        });
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar solicitação:', error);
        this.errorMessage = 'Erro ao carregar solicitação';
        this.loading = false;
      }
    });
  }

  atualizarSolicitacao() {
    if (this.solicitacaoForm.invalid) {
      this.solicitacaoForm.markAllAsTouched();
      return;
    }

    if (!this.solicitacaoId) {
      this.errorMessage = 'ID da solicitação não encontrado';
      return;
    }

    this.salvando = true;
    this.successMessage = '';
    this.errorMessage = '';

    const dados: SolicitacaoServico = this.solicitacaoForm.value;

    this.solicitacaoService.atualizar(this.solicitacaoId, dados).subscribe({
      next: () => {
        this.successMessage = 'Solicitação atualizada com sucesso!';
        this.salvando = false;
        setTimeout(() => {
          this.successMessage = '';
          this.router.navigate(['/cliente/dashboard']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('Erro ao atualizar solicitação:', error);
        this.errorMessage = error.error?.message || 'Erro ao atualizar solicitação';
        this.salvando = false;
      }
    });
  }

  voltar() {
    this.router.navigate(['/cliente/dashboard']);
  }
}
