import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-seja-profissional',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col">
      <!-- Navbar Simplificada -->
      <nav class="bg-white border-b border-gray-200 sticky top-0 z-50">
        <div class="container mx-auto px-4 h-16 flex items-center justify-between">
          <a routerLink="/" class="text-2xl font-bold text-blue-600 flex items-center gap-2">
            <span class="text-3xl">🛠️</span> VemResolver
          </a>
          <a routerLink="/login" class="text-gray-600 hover:text-blue-600 font-medium">Entrar</a>
        </div>
      </nav>

      <!-- Hero Section -->
      <section class="bg-blue-600 text-white py-20 lg:py-24 relative overflow-hidden">
        <div class="container mx-auto px-4 relative z-10">
          <div class="flex flex-col lg:flex-row items-center">
            <div class="lg:w-1/2 mb-10 lg:mb-0">
              <h1 class="text-4xl md:text-5xl lg:text-6xl font-extrabold mb-6 leading-tight">
                Consiga mais clientes e aumente sua renda
              </h1>
              <p class="text-xl md:text-2xl text-blue-100 mb-8 max-w-lg">
                Milhares de pessoas procuram profissionais como você todos os dias no VemResolver.
              </p>
              <div class="flex flex-col sm:flex-row gap-4">
                <a routerLink="/register" [queryParams]="{tipo: 'PRESTADOR'}" class="bg-white text-blue-600 px-8 py-4 rounded-lg font-bold text-lg hover:bg-gray-100 transition text-center shadow-lg transform hover:-translate-y-1">
                  Cadastre-se Grátis
                </a>
              </div>
              <p class="mt-4 text-sm text-blue-200 opacity-80">* Não cobramos mensalidade</p>
            </div>
            
            <div class="lg:w-1/2 flex justify-center lg:justify-end">
               <!-- Ilustração CSS/SVG -->
               <div class="relative w-full max-w-md aspect-square bg-blue-500 rounded-full flex items-center justify-center shadow-2xl border-8 border-blue-400">
                  <mat-icon class="text-white scale-[5] opacity-20">handyman</mat-icon>
                  <div class="absolute inset-0 flex items-center justify-center">
                     <div class="bg-white p-6 rounded-2xl shadow-xl transform rotate-3 max-w-[200px] text-gray-800">
                        <div class="flex items-center gap-2 mb-3">
                           <div class="bg-green-100 p-2 rounded-full text-green-600"><mat-icon>check</mat-icon></div>
                           <span class="font-bold">Novo Pedido!</span>
                        </div>
                        <p class="text-sm">João precisa de um Eletricista no Centro.</p>
                     </div>
                  </div>
               </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Como Funciona -->
      <section class="py-20 bg-white">
        <div class="container mx-auto px-4">
          <h2 class="text-3xl font-bold text-center text-gray-900 mb-16">Como funciona para quem trabalha?</h2>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-12 max-w-6xl mx-auto">
            <div class="text-center">
              <div class="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6 text-blue-600">
                <mat-icon class="text-4xl w-10 h-10">smartphone</mat-icon>
              </div>
              <h3 class="text-xl font-bold mb-3">1. Baixe o App ou Acesse o Site</h3>
              <p class="text-gray-600">Faça seu cadastro gratuitamente e escolha as categorias de serviço que você atende.</p>
            </div>

            <div class="text-center">
              <div class="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6 text-blue-600">
                <mat-icon class="text-4xl w-10 h-10">visibility</mat-icon>
              </div>
              <h3 class="text-xl font-bold mb-3">2. Escolha os Pedidos</h3>
              <p class="text-gray-600">Você recebe notificações de novos clientes. Veja o que eles precisam e decida quais atender.</p>
            </div>

            <div class="text-center">
              <div class="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6 text-blue-600">
                <mat-icon class="text-4xl w-10 h-10">attach_money</mat-icon>
              </div>
              <h3 class="text-xl font-bold mb-3">3. Negocie e Receba</h3>
              <p class="text-gray-600">Libere o contato do cliente, negocie diretamente com ele e fique com 100% do valor do serviço.</p>
            </div>
          </div>
        </div>
      </section>

      <!-- Quanto Custa -->
      <section class="py-20 bg-gray-50">
        <div class="container mx-auto px-4 max-w-4xl text-center">
          <h2 class="text-3xl font-bold text-gray-900 mb-8">Quanto custa para usar o VemResolver?</h2>
          <div class="bg-white p-8 md:p-12 rounded-2xl shadow-sm border border-gray-100">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
              <div class="text-left">
                <h3 class="text-2xl font-bold text-blue-600 mb-4">Sem mensalidade</h3>
                <ul class="space-y-4 text-gray-600">
                  <li class="flex items-center gap-3"><mat-icon class="text-green-500">check_circle</mat-icon> Cadastro Gratuito</li>
                  <li class="flex items-center gap-3"><mat-icon class="text-green-500">check_circle</mat-icon> Sem taxa de adesão</li>
                  <li class="flex items-center gap-3"><mat-icon class="text-green-500">check_circle</mat-icon> Sem comissão sobre o serviço</li>
                </ul>
              </div>
              <div class="bg-blue-50 p-6 rounded-xl text-left">
                <h4 class="font-bold text-gray-900 mb-2">Modelo de Moedas</h4>
                <p class="text-sm text-gray-600 mb-4">Você compra pacotes de moedas e usa apenas para liberar o contato dos clientes que te interessam.</p>
                <div class="flex items-baseline gap-1">
                  <span class="text-sm text-gray-500">A partir de</span>
                  <span class="text-3xl font-bold text-gray-900">R$ 29,90</span>
                </div>
                <p class="text-xs text-gray-400 mt-1">Pacotes acessíveis para todos os bolsos</p>
              </div>
            </div>
          </div>
          <div class="mt-10">
            <a routerLink="/register" [queryParams]="{tipo: 'PRESTADOR'}" class="bg-blue-600 text-white px-10 py-4 rounded-full font-bold text-lg hover:bg-blue-700 transition shadow-lg inline-flex items-center gap-2">
              Quero ser um Profissional <mat-icon>arrow_forward</mat-icon>
            </a>
          </div>
        </div>
      </section>

      <!-- Footer Simples -->
      <footer class="bg-white py-8 border-t border-gray-200 mt-auto">
        <div class="container mx-auto px-4 text-center text-gray-500 text-sm">
          <p class="mb-2">&copy; 2026 VemResolver LTDA.</p>
          <div class="flex justify-center gap-4">
            <a routerLink="/termos-uso" target="_blank" class="hover:text-blue-600">Termos de Uso</a>
            <a routerLink="/politica-privacidade" target="_blank" class="hover:text-blue-600">Política de Privacidade</a>
          </div>
        </div>
      </footer>
    </div>
  `
})
export class SejaProfissionalComponent {}