import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { PrestadorService } from '../services/prestador.service';
import { CategoriaService } from '../services/categoria.service';
import { AuthService } from '../services/auth.service';
import { Prestador, Categoria } from '../models/models';

@Component({
  selector: 'app-prestador-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-8">
      <div class="container mx-auto px-4 max-w-3xl">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900">Meu Perfil Profissional</h1>
          <p class="text-gray-600 mt-2">Complete seu perfil para receber mais solicitações</p>
        </div>

        <!-- Form Card -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <form [formGroup]="perfilForm" (ngSubmit)="salvarPerfil()">
            
            <!-- Descrição do Perfil -->
            <div class="mb-6">
              <label class="block text-gray-700 font-semibold mb-2">
                Sobre Você
                <span class="text-red-500">*</span>
              </label>
              <textarea
                formControlName="descricaoPerfil"
                rows="5"
                placeholder="Conte um pouco sobre sua experiência, habilidades e diferenciais..."
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                [class.border-red-500]="perfilForm.get('descricaoPerfil')?.invalid && perfilForm.get('descricaoPerfil')?.touched">
              </textarea>
              @if (perfilForm.get('descricaoPerfil')?.invalid && perfilForm.get('descricaoPerfil')?.touched) {
                <p class="text-red-500 text-sm mt-1">Descreva seu perfil profissional (mínimo 50 caracteres)</p>
              }
            </div>

            <!-- Categorias de Serviços -->
            <div class="mb-6">
              <label class="block text-gray-700 font-semibold mb-3">
                Categorias de Serviço
                <span class="text-red-500">*</span>
              </label>
              <p class="text-gray-600 text-sm mb-3">Selecione as categorias em que você atua</p>
              
              <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                @for (categoria of categorias; track categoria.id) {
                  <label class="flex items-center p-3 border rounded-lg cursor-pointer hover:bg-blue-50 transition"
                         [class.bg-blue-50]="isCategoriaSelected(categoria.id)"
                         [class.border-blue-500]="isCategoriaSelected(categoria.id)">
                    <input
                      type="checkbox"
                      [value]="categoria.id"
                      (change)="onCategoriaChange($event, categoria.id)"
                      [checked]="isCategoriaSelected(categoria.id)"
                      class="w-5 h-5 text-blue-600 rounded focus:ring-blue-500">
                    <div class="ml-3">
                      <span class="font-medium text-gray-800">{{ categoria.nome }}</span>
                      @if (categoria.descricao) {
                        <p class="text-xs text-gray-500">{{ categoria.descricao }}</p>
                      }
                    </div>
                  </label>
                }
              </div>
              @if (categoriasInvalidas) {
                <p class="text-red-500 text-sm mt-2">Selecione pelo menos uma categoria</p>
              }
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
                  [class.border-red-500]="perfilForm.get('cidade')?.invalid && perfilForm.get('cidade')?.touched">
                @if (perfilForm.get('cidade')?.invalid && perfilForm.get('cidade')?.touched) {
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
                  [class.border-red-500]="perfilForm.get('estado')?.invalid && perfilForm.get('estado')?.touched">
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
                @if (perfilForm.get('estado')?.invalid && perfilForm.get('estado')?.touched) {
                  <p class="text-red-500 text-sm mt-1">Estado é obrigatório</p>
                }
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
                (click)="voltarDashboard()"
                class="px-6 py-3 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition">
                Voltar
              </button>
              
              <button
                type="submit"
                [disabled]="loading"
                class="px-8 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
                @if (loading) {
                  <span>Salvando...</span>
                } @else {
                  <span>Salvar Perfil</span>
                }
              </button>
            </div>
          </form>
        </div>

        <!-- Progress Indicator -->
        <div class="mt-8 bg-white rounded-lg shadow-md p-6">
          <h3 class="font-semibold text-gray-900 mb-4">Completude do Perfil</h3>
          <div class="w-full bg-gray-200 rounded-full h-3">
            <div 
              class="bg-blue-600 h-3 rounded-full transition-all duration-500"
              [style.width.%]="calcularCompletude()">
            </div>
          </div>
          <p class="text-sm text-gray-600 mt-2">{{ calcularCompletude() }}% completo</p>
        </div>
      </div>
    </div>
  `
})
export class PrestadorPerfilComponent implements OnInit {
  perfilForm: FormGroup;
  categorias: Categoria[] = [];
  categoriasSelecionadas: number[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';
  categoriasInvalidas = false;
  prestadorExistente?: Prestador;

  constructor(
    private fb: FormBuilder,
    private prestadorService: PrestadorService,
    private categoriaService: CategoriaService,
    private authService: AuthService,
    private router: Router
  ) {
    this.perfilForm = this.fb.group({
      descricaoPerfil: ['', [Validators.required, Validators.minLength(50)]],
      cidade: ['', Validators.required],
      estado: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
    this.carregarPerfilExistente();
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 100).subscribe({
      next: (response) => {
        this.categorias = response.content;
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
        this.errorMessage = 'Erro ao carregar categorias';
      }
    });
  }

  carregarPerfilExistente() {
    const userId = this.authService.getCurrentUserId();
    if (!userId) return;

    this.prestadorService.buscarPorUsuario(userId).subscribe({
      next: (prestador: Prestador) => {
        this.prestadorExistente = prestador;
        this.perfilForm.patchValue({
          descricaoPerfil: prestador.descricaoPerfil || '',
          cidade: prestador.cidade || '',
          estado: prestador.estado || ''
        });
        this.categoriasSelecionadas = prestador.categoriasIds || [];
      },
      error: (error: any) => {
        console.log('Nenhum perfil encontrado, criando novo');
      }
    });
  }

  isCategoriaSelected(categoriaId: number): boolean {
    return this.categoriasSelecionadas.includes(categoriaId);
  }

  onCategoriaChange(event: any, categoriaId: number) {
    if (event.target.checked) {
      this.categoriasSelecionadas.push(categoriaId);
    } else {
      const index = this.categoriasSelecionadas.indexOf(categoriaId);
      if (index > -1) {
        this.categoriasSelecionadas.splice(index, 1);
      }
    }
    this.categoriasInvalidas = false;
  }

  calcularCompletude(): number {
    let pontos = 0;
    const total = 4;

    if (this.perfilForm.get('descricaoPerfil')?.valid) pontos++;
    if (this.perfilForm.get('cidade')?.valid) pontos++;
    if (this.perfilForm.get('estado')?.valid) pontos++;
    if (this.categoriasSelecionadas.length > 0) pontos++;

    return Math.round((pontos / total) * 100);
  }

  salvarPerfil() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.perfilForm.invalid) {
      Object.keys(this.perfilForm.controls).forEach(key => {
        this.perfilForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Por favor, preencha todos os campos obrigatórios';
      return;
    }

    if (this.categoriasSelecionadas.length === 0) {
      this.categoriasInvalidas = true;
      this.errorMessage = 'Selecione pelo menos uma categoria de serviço';
      return;
    }

    this.loading = true;
    const userId = this.authService.getCurrentUserId();

    const prestadorData: Prestador = {
      id: this.prestadorExistente?.id || 0,
      usuarioId: userId!,
      categoriasIds: this.categoriasSelecionadas,
      descricaoPerfil: this.perfilForm.value.descricaoPerfil,
      cidade: this.perfilForm.value.cidade,
      estado: this.perfilForm.value.estado
    };

    const request = this.prestadorExistente
      ? this.prestadorService.atualizar(this.prestadorExistente.id, prestadorData)
      : this.prestadorService.criar(prestadorData);

    request.subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Perfil salvo com sucesso!';
        setTimeout(() => {
          this.router.navigate(['/prestador/dashboard']);
        }, 2000);
      },
      error: (error: any) => {
        this.loading = false;
        console.error('Erro ao salvar perfil:', error);
        this.errorMessage = 'Erro ao salvar perfil. Tente novamente.';
      }
    });
  }

  voltarDashboard() {
    this.router.navigate(['/prestador/dashboard']);
  }
}
