import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CategoriaService } from '../services/categoria.service';
import { Categoria } from '../models/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <!-- Hero Section -->
    <section class="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-20">
      <div class="container mx-auto px-4">
        <div class="max-w-3xl mx-auto text-center">
          <h1 class="text-5xl font-bold mb-6">Encontre o profissional ideal para seu projeto</h1>
          <p class="text-xl mb-8">Milhares de prestadores qualificados prontos para atender você</p>
          
          <!-- Search Bar -->
          <div class="bg-white rounded-lg shadow-lg p-2 flex flex-col md:flex-row gap-2">
            <input 
              type="text" 
              [(ngModel)]="searchTerm"
              placeholder="Qual serviço você precisa?"
              class="flex-1 px-4 py-3 rounded-md text-gray-800 focus:outline-none"
            >
            <button 
              (click)="buscarServico()"
              class="bg-blue-600 text-white px-8 py-3 rounded-md hover:bg-blue-700 font-semibold transition">
              Buscar
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Categorias Populares -->
    <section class="py-16 bg-gray-50">
      <div class="container mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-12">Categorias Populares</h2>
        
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          <div 
            *ngFor="let categoria of categorias" 
            class="bg-white rounded-lg shadow-md hover:shadow-xl transition cursor-pointer p-6 text-center"
            (click)="selecionarCategoria(categoria)">
            <div class="text-4xl mb-3">{{ getIconeCategoria(categoria.nome) }}</div>
            <h3 class="font-semibold text-gray-800">{{ categoria.nome }}</h3>
          </div>
        </div>
      </div>
    </section>

    <!-- Como Funciona -->
    <section class="py-16">
      <div class="container mx-auto px-4">
        <h2 class="text-3xl font-bold text-center mb-12">Como Funciona</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          <div class="text-center">
            <div class="bg-blue-100 rounded-full w-20 h-20 flex items-center justify-center mx-auto mb-4">
              <span class="text-3xl">📝</span>
            </div>
            <h3 class="text-xl font-semibold mb-3">1. Descreva seu projeto</h3>
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

    <!-- Benefícios -->
    <section class="py-16 bg-gray-50">
      <div class="container mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-12 max-w-5xl mx-auto">
          <div>
            <h2 class="text-3xl font-bold mb-8">Para Clientes</h2>
            <ul class="space-y-4">
              <li class="flex items-start">
                <span class="text-green-500 mr-3 text-xl">✓</span>
                <div>
                  <strong>Múltiplas propostas:</strong> Receba vários orçamentos e compare
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-3 text-xl">✓</span>
                <div>
                  <strong>Profissionais verificados:</strong> Prestadores avaliados por outros clientes
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-3 text-xl">✓</span>
                <div>
                  <strong>Sem compromisso:</strong> Você escolhe se e quando contratar
                </div>
              </li>
            </ul>
            <button 
              routerLink="/register"
              class="mt-6 border-2 border-blue-600 text-blue-600 px-6 py-3 rounded-lg font-semibold hover:bg-blue-50 transition">
              Publicar Solicitação
            </button>
          </div>

          <div>
            <h2 class="text-3xl font-bold mb-8">Para Prestadores</h2>
            <ul class="space-y-4">
              <li class="flex items-start">
                <span class="text-green-500 mr-3 text-xl">✓</span>
                <div>
                  <strong>Novos clientes:</strong> Acesse milhares de oportunidades
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-3 text-xl">✓</span>
                <div>
                  <strong>Sem intermediários:</strong> Negocie diretamente com o cliente
                </div>
              </li>
              <li class="flex items-start">
                <span class="text-green-500 mr-3 text-xl">✓</span>
                <div>
                  <strong>Construa reputação:</strong> Receba avaliações e cresça
                </div>
              </li>
            </ul>
            <button 
              routerLink="/register"
              class="mt-6 bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition">
              Cadastrar como Prestador
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-800 text-white py-8">
      <div class="container mx-auto px-4">
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
              <li><a href="#" class="hover:text-white">Termos de uso</a></li>
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
  `]
})
export class HomeComponent implements OnInit {
  categorias: Categoria[] = [];
  searchTerm: string = '';

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit() {
    this.carregarCategorias();
  }

  carregarCategorias() {
    this.categoriaService.listarTodas(0, 10).subscribe({
      next: (response) => {
        this.categorias = response.content;
      },
      error: (error) => {
        console.error('Erro ao carregar categorias:', error);
      }
    });
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
}
