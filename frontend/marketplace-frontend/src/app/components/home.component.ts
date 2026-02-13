import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CategoriaService } from '../services/categoria.service';
import { Categoria } from '../models/models';
import { MatIconModule } from '@angular/material/icon';
import { MaskDirective } from '../directives/mask.directive';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule, MatIconModule, MaskDirective],
  template: `
    <!-- Nova Hero Section (Estilo GetNinjas) -->
    <section class="bg-white pt-12 pb-16 overflow-hidden relative">
      <div class="container max-w-6xl mx-auto px-4">
        <div class="flex flex-col lg:flex-row items-center">
          
          <!-- Lado Esquerdo: Texto e Busca -->
          <div class="lg:w-3/5 z-10">
            <h1 class="text-4xl md:text-5xl lg:text-[3.5rem] font-bold text-gray-900 leading-tight mb-4">
              Mais de 500 tipos de serviços<br>em um só lugar.
            </h1>
            <p class="text-xl text-gray-600 mb-10">
              Encontre profissionais e contrate serviços para tudo o que precisar
            </p>

            <!-- Barra de Busca -->
            <div class="relative w-full max-w-3xl shadow-lg rounded-lg border border-gray-200 bg-white flex items-center p-2 mb-6 transition-shadow hover:shadow-xl">
              <mat-icon class="text-blue-400 ml-3 mr-3 scale-125">search</mat-icon>
              <input 
                type="text" 
                [(ngModel)]="searchTerm"
                (keyup.enter)="buscarServico()"
                placeholder="O que você precisa?"
                class="flex-1 text-gray-700 text-lg outline-none placeholder-gray-400 h-12"
              >
              <button 
                *ngIf="searchTerm" 
                (click)="searchTerm = ''" 
                class="text-blue-300 hover:text-blue-500 mr-4">
                <mat-icon>close</mat-icon>
              </button>
            </div>

            <!-- Tags Populares -->
            <div class="flex flex-wrap items-center gap-2 text-sm text-gray-700">
              <span class="font-bold mr-1">Populares:</span>
              <button (click)="searchTerm='Diarista'; buscarServico()" class="px-4 py-1.5 border border-gray-300 rounded-full hover:bg-gray-50 text-gray-600 transition">Diarista</button>
              <button (click)="searchTerm='Pedreiro'; buscarServico()" class="px-4 py-1.5 border border-gray-300 rounded-full hover:bg-gray-50 text-gray-600 transition">Pedreiro</button>
              <button (click)="searchTerm='Mudanças'; buscarServico()" class="px-4 py-1.5 border border-gray-300 rounded-full hover:bg-gray-50 text-gray-600 transition">Mudanças</button>
              <button (click)="searchTerm='Eletricista'; buscarServico()" class="px-4 py-1.5 border border-gray-300 rounded-full hover:bg-gray-50 text-gray-600 transition">Eletricista</button>
              <button (click)="searchTerm='Montador de Móveis'; buscarServico()" class="px-4 py-1.5 border border-gray-300 rounded-full hover:bg-gray-50 text-gray-600 transition">Montador de Móveis</button>
            </div>
          </div>

          <!-- Lado Direito: Imagem do Profissional (Sem Card Rogério) -->
          <div class="lg:w-2/5 mt-12 lg:mt-0 relative flex justify-center lg:justify-end">
            <div class="relative">
              <!-- Imagem Profissional -->
              <img 
                src="https://images.unsplash.com/photo-1503387762-592deb58ef4e?q=80&w=600&auto=format&fit=crop" 
                alt="Profissional com ferramentas" 
                class="rounded-lg shadow-2xl object-cover h-[450px] w-full max-w-sm z-10 relative"
              >
            </div>
          </div>

        </div>
      </div>
    </section>

    <!-- Banner do App -->
    <section class="py-12 bg-gray-50 overflow-hidden border-t border-gray-100">
      <div class="container max-w-6xl mx-auto px-4">
        <div class="flex flex-col md:flex-row items-center justify-between max-w-6xl mx-auto">
          <!-- Texto e Botões -->
          <div class="md:w-1/2 mb-10 md:mb-0 z-10 text-center md:text-left">
            <span class="text-blue-600 font-bold tracking-wider uppercase text-sm mb-2 block">Baixe nosso App</span>
            <h2 class="text-3xl font-bold text-gray-900 mb-4 leading-tight">Resolva tudo na palma da sua mão</h2>
            <p class="text-gray-600 mb-6 max-w-md mx-auto md:mx-0">
              Tenha acesso aos melhores profissionais e gerencie seus pedidos de onde estiver.
            </p>
            
            <div class="flex flex-col sm:flex-row gap-4 justify-center md:justify-start">
              <a href="#" class="transition-transform hover:-translate-y-1 inline-block">
                <img src="https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg" alt="Disponível no Google Play" class="h-12">
              </a>
              <a href="#" class="transition-transform hover:-translate-y-1 inline-block">
                <img src="https://upload.wikimedia.org/wikipedia/commons/3/3c/Download_on_the_App_Store_Badge.svg" alt="Baixar na App Store" class="h-12">
              </a>
            </div>
          </div>

          <!-- Imagem do Celular (Mockup CSS/HTML) -->
          <div class="md:w-1/2 flex justify-center md:justify-end relative mt-4 md:mt-0">
            <!-- Mockup Celular Simplificado -->
            <div class="relative z-10 bg-gray-900 rounded-[2rem] border-[6px] border-gray-900 w-48 h-[350px] shadow-xl overflow-hidden transform rotate-[-5deg]">
              <div class="bg-gray-50 w-full h-full relative overflow-hidden">
                <div class="bg-blue-600 h-14 w-full absolute top-0 left-0"></div>
                <div class="p-3 mt-14 space-y-3">
                   <div class="h-24 bg-white rounded-lg shadow-sm w-full"></div>
                   <div class="h-24 bg-white rounded-lg shadow-sm w-full opacity-60"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Categorias Populares (Carrossel Infinito) -->
    <section id="categorias" class="py-16 bg-white relative">
      <div class="container max-w-6xl mx-auto px-4 relative group/slider">
        <h2 class="text-3xl font-bold text-center mb-12 text-gray-800">Categorias Populares</h2>
        
        <div class="relative max-w-7xl mx-auto">
          <!-- Botão Esquerdo -->
          <button 
            (click)="scrollLeft()"
            class="absolute left-0 top-1/2 -translate-y-1/2 -ml-4 lg:-ml-12 z-20 bg-white shadow-lg rounded-full p-3 text-blue-600 hover:text-blue-800 hover:scale-110 transition opacity-0 group-hover/slider:opacity-100 disabled:opacity-0 focus:outline-none">
            <mat-icon>chevron_left</mat-icon>
          </button>

          <!-- Container Scroll -->
          <div #categoriesContainer class="flex overflow-x-auto gap-6 pb-8 scroll-smooth hide-scrollbar px-4" style="scroll-behavior: smooth;">
            @for (categoria of categorias; track categoria.id) {
              <div 
                class="flex-shrink-0 w-64 h-48 bg-white border border-gray-100 rounded-xl shadow-md hover:shadow-xl hover:-translate-y-1 transition-all cursor-pointer p-6 flex flex-col items-center justify-center text-center group"
                (click)="selecionarCategoria(categoria)">
                <div class="text-5xl mb-4 transform group-hover:scale-110 transition duration-300">{{ getIconeCategoria(categoria.nome) }}</div>
                <h3 class="font-bold text-lg text-gray-800 group-hover:text-blue-600 transition">{{ categoria.nome }}</h3>
                <p class="text-xs text-gray-400 mt-2">{{ categoria.descricao }}</p>
              </div>
            }
          </div>

          <!-- Botão Direito -->
          <button 
            (click)="scrollRight()"
            class="absolute right-0 top-1/2 -translate-y-1/2 -mr-4 lg:-mr-12 z-20 bg-white shadow-lg rounded-full p-3 text-blue-600 hover:text-blue-800 hover:scale-110 transition opacity-0 group-hover/slider:opacity-100 focus:outline-none">
            <mat-icon>chevron_right</mat-icon>
          </button>
        </div>
      </div>
    </section>

    <!-- Como Funciona (Clientes) - MOVIDO PARA CÁ -->
    <section id="como-funciona" class="py-16 bg-white border-t border-gray-100">
      <div class="container max-w-6xl mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-12">Como Funciona para Clientes</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          <div class="text-center">
            <div class="bg-blue-100 rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">📝</span>
            </div>
            <h3 class="text-xl font-semibold mb-3">1. Descreva seu serviço</h3>
            <p class="text-gray-600">Conte-nos o que você precisa em poucos minutos</p>
          </div>

          <div class="text-center">
            <div class="bg-blue-100 rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">💼</span>
            </div>
            <h3 class="text-xl font-semibold mb-3">2. Receba propostas</h3>
            <p class="text-gray-600">Prestadores qualificados enviam orçamentos</p>
          </div>

          <div class="text-center">
            <div class="bg-blue-100 rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">✅</span>
            </div>
            <h3 class="text-xl font-semibold mb-3">3. Contrate com segurança</h3>
            <p class="text-gray-600">Compare, escolha e contrate o melhor profissional</p>
          </div>
        </div>

        <div class="text-center mt-12">
          <button 
            routerLink="/register"
            class="bg-blue-600 text-white px-8 py-4 rounded-lg text-lg font-semibold hover:bg-blue-700 transition">
            Começar Agora - É Grátis
          </button>
        </div>
      </div>
    </section>

    <!-- Seção Profissional (Estilo GetNinjas) -->
    <section class="py-16 bg-gray-50 border-t border-gray-200">
      <div class="container max-w-6xl mx-auto px-4">
        
        <!-- Caixa de Cadastro Rápido -->
        <div class="bg-blue-600 rounded-xl p-8 md:p-12 shadow-md max-w-5xl mx-auto mb-20 relative overflow-hidden">
          <!-- Decorativo de fundo -->
          <div class="absolute -right-10 -top-10 text-white opacity-10 transform rotate-12 pointer-events-none">
            <mat-icon style="font-size: 150px; height: 150px; width: 150px;">construction</mat-icon>
          </div>

          <div class="relative z-10">
            <h2 class="text-3xl font-bold text-white mb-2">Cadastre-se como Profissional VemResolver</h2>
            <p class="text-blue-100 mb-8 text-lg">Preencha os campos abaixo:</p>

            <form [formGroup]="quickRegisterForm" (ngSubmit)="iniciarCadastro()" class="flex flex-col md:flex-row gap-4 items-start">
              <div class="flex-1 w-full">
                <label class="block text-sm font-medium text-blue-50 mb-1 ml-1">
                  E-mail <span class="text-red-300 font-bold">*</span>
                </label>
                <input 
                  type="email" 
                  formControlName="email"
                  placeholder="Ex.: email@email.com.br" 
                  class="w-full px-4 py-3 rounded-lg border-0 focus:ring-2 focus:ring-yellow-400 shadow-sm bg-white text-gray-900"
                  [class.ring-2]="quickRegisterForm.get('email')?.invalid && quickRegisterForm.get('email')?.touched"
                  [class.ring-red-400]="quickRegisterForm.get('email')?.invalid && quickRegisterForm.get('email')?.touched"
                >
                <div *ngIf="quickRegisterForm.get('email')?.invalid && quickRegisterForm.get('email')?.touched" class="text-red-200 text-xs mt-1 ml-1 font-medium">
                  <span *ngIf="quickRegisterForm.get('email')?.hasError('required')">O e-mail é obrigatório.</span>
                  <span *ngIf="quickRegisterForm.get('email')?.hasError('email')">Digite um e-mail válido.</span>
                </div>
              </div>
              
              <div class="flex-1 w-full">
                <label class="block text-sm font-medium text-blue-50 mb-1 ml-1">
                  Celular <span class="text-red-300 font-bold">*</span>
                </label>
                <input 
                  type="tel" 
                  formControlName="celular"
                  appMask="(00) 00000-0000"
                  maxlength="15"
                  placeholder="Ex.: (11) 99999-9999" 
                  class="w-full px-4 py-3 rounded-lg border-0 focus:ring-2 focus:ring-yellow-400 shadow-sm bg-white text-gray-900"
                  [class.ring-2]="quickRegisterForm.get('celular')?.invalid && quickRegisterForm.get('celular')?.touched"
                  [class.ring-red-400]="quickRegisterForm.get('celular')?.invalid && quickRegisterForm.get('celular')?.touched"
                >
                <div *ngIf="quickRegisterForm.get('celular')?.invalid && quickRegisterForm.get('celular')?.touched" class="text-red-200 text-xs mt-1 ml-1 font-medium">
                  <span *ngIf="quickRegisterForm.get('celular')?.hasError('required')">O celular é obrigatório.</span>
                  <span *ngIf="quickRegisterForm.get('celular')?.hasError('minlength')">Celular incompleto.</span>
                </div>
              </div>

              <button 
                type="submit"
                class="w-full md:w-auto bg-white text-blue-700 px-8 py-3 rounded-lg font-bold text-lg hover:bg-gray-100 transition shadow-md h-[48px] md:min-w-[200px] mt-0 md:mt-[2px]">
                Cadastre-se
              </button>
            </form>
          </div>
        </div>

        <!-- Como Funciona para Profissionais (Ícones) -->
        <div class="max-w-6xl mx-auto">
          <h2 class="text-3xl font-bold text-center mb-16 text-gray-900">Como o VemResolver funciona para o Profissional?</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 text-center px-4">
            <!-- Passo 1 -->
            <div class="flex flex-col items-center group">
              <div class="w-24 h-32 flex items-center justify-center mb-6 relative">
                 <!-- Ícone Celular -->
                 <div class="border-2 border-blue-900 rounded-xl w-16 h-28 relative bg-white group-hover:-translate-y-2 transition-transform duration-300 shadow-sm">
                    <div class="w-1 h-1 bg-blue-900 rounded-full mx-auto mt-2"></div>
                    <div class="absolute bottom-2 left-0 right-0 mx-auto w-8 h-1 bg-blue-400 rounded-full opacity-50"></div>
                 </div>
              </div>
              <p class="text-gray-600 leading-relaxed px-2">Pedidos de serviços chegando toda hora no seu celular.</p>
            </div>

            <!-- Passo 2 -->
            <div class="flex flex-col items-center group">
              <div class="w-24 h-32 flex items-center justify-center mb-6">
                <!-- Ícone Lista/Tela -->
                 <div class="border-2 border-blue-900 rounded-lg w-20 h-24 bg-white relative transform rotate-3 group-hover:rotate-6 transition-transform duration-300 shadow-sm">
                    <div class="space-y-2 p-3">
                       <div class="h-1 bg-blue-100 rounded w-full"></div>
                       <div class="h-1 bg-blue-100 rounded w-3/4"></div>
                       <div class="h-1 bg-blue-100 rounded w-1/2"></div>
                    </div>
                    <div class="absolute -bottom-2 -right-2 w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center border-2 border-white shadow-sm">
                       <mat-icon class="text-white text-sm font-bold scale-75">touch_app</mat-icon>
                    </div>
                 </div>
              </div>
              <p class="text-gray-600 leading-relaxed px-2">Veja todos os pedidos de graça, e invista apenas nos que gostar.</p>
            </div>

            <!-- Passo 3 -->
            <div class="flex flex-col items-center group">
              <div class="w-24 h-32 flex items-center justify-center mb-6 relative">
                 <!-- Ícone Moedas -->
                 <div class="relative">
                    <div class="w-12 h-12 rounded-full border-2 border-blue-900 bg-blue-200 flex items-center justify-center absolute -top-8 -left-6 z-0">
                       <div class="w-8 h-8 border border-blue-400 rounded-full opacity-50"></div>
                    </div>
                    <div class="w-14 h-14 rounded-full border-2 border-blue-900 bg-blue-600 flex items-center justify-center relative z-10 group-hover:scale-110 transition-transform shadow-sm">
                       <span class="text-white font-bold text-xl">★</span>
                    </div>
                 </div>
              </div>
              <p class="text-gray-600 leading-relaxed px-2">Compre moedas no aplicativo para desbloquear os contatos dos clientes.</p>
            </div>

            <!-- Passo 4 -->
            <div class="flex flex-col items-center group">
              <div class="w-24 h-32 flex items-center justify-center mb-6">
                 <!-- Ícone Dinheiro/Mão -->
                 <div class="relative">
                    <mat-icon class="text-6xl text-blue-900 h-16 w-16">payments</mat-icon>
                    <div class="absolute -top-2 -right-2 bg-blue-600 rounded-full w-8 h-8 flex items-center justify-center border-2 border-white font-bold text-white shadow-sm">$</div>
                 </div>
              </div>
              <p class="text-gray-600 leading-relaxed px-2">100% do valor do serviço é seu. E sem mensalidade!</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Dicas de Segurança (Nova Seção) -->
    <section class="py-16 bg-white border-t border-gray-200">
      <div class="container max-w-6xl mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-12 text-gray-800">Dicas Antes de Contratar</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          
          <div class="bg-gray-50 rounded-xl p-6 hover:shadow-lg transition border border-gray-100">
            <div class="text-blue-600 mb-4">
              <mat-icon class="text-4xl w-10 h-10">verified_user</mat-icon>
            </div>
            <h3 class="font-bold text-lg mb-2 text-gray-900">Verifique Avaliações</h3>
            <p class="text-gray-600 text-sm">
              Sempre confira as estrelas e comentários de clientes anteriores para garantir a qualidade do serviço.
            </p>
          </div>

          <div class="bg-gray-50 rounded-xl p-6 hover:shadow-lg transition border border-gray-100">
            <div class="text-blue-600 mb-4">
              <mat-icon class="text-4xl w-10 h-10">compare_arrows</mat-icon>
            </div>
            <h3 class="font-bold text-lg mb-2 text-gray-900">Compare Orçamentos</h3>
            <p class="text-gray-600 text-sm">
              Não feche com o primeiro. Solicite ao menos 3 orçamentos para ter uma base justa de preço.
            </p>
          </div>

          <div class="bg-gray-50 rounded-xl p-6 hover:shadow-lg transition border border-gray-100">
            <div class="text-blue-600 mb-4">
              <mat-icon class="text-4xl w-10 h-10">chat</mat-icon>
            </div>
            <h3 class="font-bold text-lg mb-2 text-gray-900">Combine Tudo Antes</h3>
            <p class="text-gray-600 text-sm">
              Deixe claro no chat o que será feito, prazos e valores. Mantenha toda a negociação registrada.
            </p>
          </div>

          <div class="bg-gray-50 rounded-xl p-6 hover:shadow-lg transition border border-gray-100">
            <div class="text-blue-600 mb-4">
              <mat-icon class="text-4xl w-10 h-10">payments</mat-icon>
            </div>
            <h3 class="font-bold text-lg mb-2 text-gray-900">Pagamento Seguro</h3>
            <p class="text-gray-600 text-sm">
              Evite pagamentos antecipados fora da plataforma. Pague apenas conforme o combinado e seguro.
            </p>
          </div>

        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-800 text-white py-8">
      <div class="container max-w-6xl mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <h3 class="font-bold text-lg mb-4">Marketplace de Serviços</h3>
            <p class="text-gray-400">Conectando pessoas aos melhores profissionais</p>
          </div>
          <div>
            <h3 class="font-bold text-lg mb-4">Links Rápidos</h3>
            <ul class="space-y-2 text-gray-400">
              <li><a href="#" class="hover:text-white">Sobre nós</a></li>
              <li><a href="#" class="hover:text-white">Como funciona</a></li>
              <li><a routerLink="/termos-uso" target="_blank" class="hover:text-white cursor-pointer">Termos de uso</a></li>
              <li><a routerLink="/politica-privacidade" target="_blank" class="hover:text-white cursor-pointer">Política de Privacidade</a></li>
            </ul>
          </div>
          <div>
            <h3 class="font-bold text-lg mb-4">Contato</h3>
            <ul class="space-y-2 text-gray-400">
              <li>contato&#64;marketplace.com</li>
              <li>(11) 9999-9999</li>
            </ul>
          </div>
        </div>
        <div class="border-t border-gray-700 mt-8 pt-8 text-center text-gray-400">
          <p>&copy; 2026 Marketplace de Serviços. Todos os direitos reservados.</p>
        </div>
      </div>
    </footer>
  `,
  styles: [`
    :host {
      display: block;
    }
    .hide-scrollbar::-webkit-scrollbar {
      display: none;
    }
    .hide-scrollbar {
      -ms-overflow-style: none;
      scrollbar-width: none;
    }
  `]
})
export class HomeComponent implements OnInit {
  @ViewChild('categoriesContainer') categoriesContainer!: ElementRef;
  categorias: Categoria[] = [];
  searchTerm: string = '';
  quickRegisterForm: FormGroup;

  constructor(
    private categoriaService: CategoriaService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.quickRegisterForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      celular: ['', [Validators.required, Validators.minLength(11)]]
    });
  }

  ngOnInit() {
    this.carregarCategorias();
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 50).subscribe({
      next: (response) => {
        // Duplicar conteúdo para dar sensação de volume se tiver poucas
        if (response.content.length > 0 && response.content.length < 10) {
           this.categorias = [...response.content, ...response.content, ...response.content];
        } else {
           this.categorias = response.content;
        }
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
      }
    });
  }

  scrollLeft() {
    if (this.categoriesContainer) {
      this.categoriesContainer.nativeElement.scrollLeft -= 300;
    }
  }

  scrollRight() {
    if (this.categoriesContainer) {
      this.categoriesContainer.nativeElement.scrollLeft += 300;
    }
  }

  getIconeCategoria(nome: string): string {
    const icones: { [key: string]: string } = {
      'Eletricista': '⚡',
      'Encanador': '🔧',
      'Pintor': '🎨',
      'Pedreiro': '🧱',
      'Marceneiro': '🪚',
      'Jardineiro': '🌿',
      'Diarista': '🧹',
      'Professor Particular': '📚',
      'Mecânico': '🔩',
      'Desenvolvedor': '💻'
    };
    return icones[nome] || '🛠️';
  }

  buscarServico() {
    console.log('Buscando:', this.searchTerm);
  }

  selecionarCategoria(categoria: Categoria) {
    console.log('Categoria selecionada:', categoria);
  }

  iniciarCadastro() {
    if (this.quickRegisterForm.valid) {
      this.router.navigate(['/register'], {
        queryParams: {
          tipo: 'PRESTADOR',
          email: this.quickRegisterForm.value.email,
          telefone: this.quickRegisterForm.value.celular
        }
      });
    } else {
      this.quickRegisterForm.markAllAsTouched();
    }
  }
}