import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { MENU_DATA } from '../models/menu-data';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  template: `
    <nav class="bg-white shadow-md sticky top-0 z-50">
      <!-- Main Header -->
      <div class="border-b bg-white">
        <div class="container mx-auto px-4">
          <div class="flex items-center justify-between h-16">
            <!-- Logo -->
            <div class="flex items-center">
              <a routerLink="/" class="text-2xl font-bold text-blue-600 hover:text-blue-700 transition flex items-center gap-2">
                <span class="text-3xl">🛠️</span> VemResolver
              </a>
            </div>

            <!-- User Menu -->
            <div class="flex items-center space-x-6 ml-auto">
              @if (authService.currentUser$ | async; as user) {
                <div class="flex items-center space-x-6">
                  <div class="text-right hidden md:block">
                    <p class="text-xs text-gray-500">Bem-vindo,</p>
                    <p class="text-sm font-bold text-gray-800 truncate max-w-[150px]">{{ user.nome }}</p>
                  </div>

                  @if (authService.isCliente()) {
                    <a routerLink="/cliente/dashboard" class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium text-sm shadow-sm">
                      Meu Painel
                    </a>
                  }

                  @if (authService.isPrestador()) {
                    <a routerLink="/prestador/dashboard" class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium text-sm shadow-sm">
                      Meu Painel
                    </a>
                  }
                  
                  <div class="h-8 w-[1px] bg-gray-200 mx-2"></div>

                  <button (click)="logout()" class="text-sm font-medium text-red-500 hover:text-red-700 transition flex items-center gap-1">
                    Sair
                    <mat-icon class="text-sm h-4 w-4 leading-none flex items-center">logout</mat-icon>
                  </button>
                </div>
              } @else {
                <!-- Menu Visitante (Igual à imagem) -->
                <div class="hidden md:flex items-center space-x-6">
                  <a routerLink="/register" class="bg-gray-100 text-blue-600 px-6 py-2.5 rounded-lg hover:bg-gray-200 transition font-bold text-sm">
                    Seja um profissional
                  </a>
                  
                  <a href="#como-funciona" class="text-blue-600 hover:text-blue-800 transition font-medium text-sm">
                    Como funciona?
                  </a>
                  
                  <a href="#" class="text-blue-600 hover:text-blue-800 transition font-medium text-sm">
                    Segurança
                  </a>
                  
                  <a routerLink="/login" class="flex items-center text-blue-600 hover:text-blue-800 transition font-medium text-sm gap-1">
                    <mat-icon class="text-lg h-5 w-5 leading-none flex items-center">person_outline</mat-icon>
                    Entrar
                  </a>
                </div>
              }
              
              <!-- Mobile Menu Button -->
              <button (click)="toggleMobileMenu()" class="md:hidden p-2 text-gray-600 hover:text-blue-600">
                <mat-icon>menu</mat-icon>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Categories Mega Menu (Desktop) -->
      <div class="hidden md:block border-b bg-white relative">
        <div class="container mx-auto px-4">
          <ul class="flex justify-between items-stretch">
            @for (category of menuData; track category.id) {
              <li class="group static">
                <a routerLink="/" fragment="categorias" class="flex flex-col items-center justify-center px-2 py-4 text-gray-500 hover:text-blue-600 hover:bg-blue-50 transition border-b-2 border-transparent group-hover:border-blue-600 h-full w-full min-w-[90px] cursor-pointer">
                  <img [src]="category.image" [alt]="category.label" class="w-10 h-10 mb-2 object-contain group-hover:scale-110 transition-transform duration-200 drop-shadow-sm">
                  <span class="text-[11px] font-semibold text-center uppercase tracking-wide leading-tight">{{ category.label }}</span>
                </a>
                
                <!-- Mega Menu Dropdown -->
                <div class="absolute left-0 w-full bg-white shadow-xl border-t border-gray-100 hidden group-hover:block z-50 animate-fade-in top-full pb-8 pt-6">
                   <div class="container mx-auto px-4">
                      <div class="flex mb-6 border-b pb-4 items-center">
                        <img [src]="category.image" class="w-12 h-12 mr-4 object-contain">
                        <h2 class="text-2xl font-bold text-gray-800">{{ category.label }}</h2>
                      </div>
                      <div class="grid grid-cols-4 gap-x-8 gap-y-8">
                        @for (subGroup of category.subcategories; track subGroup.title) {
                          <div>
                            <h3 class="font-bold text-gray-900 mb-4 text-sm uppercase tracking-wider border-l-4 border-blue-500 pl-3">{{ subGroup.title }}</h3>
                            <ul class="space-y-2.5">
                              @for (item of subGroup.items; track item) {
                                <li>
                                  <a href="javascript:void(0)" class="text-gray-500 hover:text-blue-600 text-sm hover:underline block py-0.5 transition-colors flex items-center group/item">
                                    <span class="w-1.5 h-1.5 bg-gray-300 rounded-full mr-2 group-hover/item:bg-blue-500 transition-colors"></span>
                                    {{ item }}
                                  </a>
                                </li>
                              }
                            </ul>
                          </div>
                        }
                      </div>
                   </div>
                </div>
              </li>
            }
          </ul>
        </div>
      </div>

      <!-- Mobile Menu -->
      @if (mobileMenuOpen) {
        <div class="md:hidden bg-white border-t max-h-[calc(100vh-64px)] overflow-y-auto">
          <div class="p-4 space-y-4">
            <!-- Mobile Search -->
             <div class="relative w-full mb-6">
                <input 
                  type="text" 
                  placeholder="Buscar serviços..." 
                  class="w-full px-4 py-3 pl-10 pr-4 rounded-lg border border-gray-300 focus:outline-none focus:border-blue-500 bg-gray-50"
                >
                <div class="absolute left-3 top-3.5 text-gray-400">
                   <mat-icon class="text-sm">search</mat-icon>
                </div>
              </div>

            <div class="font-bold text-gray-900 mb-2 uppercase text-xs tracking-wider">Navegação</div>
            <div class="flex flex-col space-y-2 pl-2">
               <a routerLink="/" (click)="mobileMenuOpen = false" class="text-gray-700 py-2 border-b border-gray-100">Início</a>
               @if (!(authService.currentUser$ | async)) {
                 <a routerLink="/login" (click)="mobileMenuOpen = false" class="text-gray-700 py-2 border-b border-gray-100">Entrar</a>
                 <a routerLink="/register" (click)="mobileMenuOpen = false" class="text-blue-600 font-bold py-2 border-b border-gray-100">Criar Conta</a>
               }
            </div>

            <div class="font-bold text-gray-900 mt-6 mb-2 uppercase text-xs tracking-wider">Categorias</div>
            <div class="space-y-1">
              @for (category of menuData; track category.id) {
                 <div class="border-b border-gray-50 last:border-0">
                    <div class="flex items-center justify-between py-3 px-2 hover:bg-gray-50 rounded cursor-pointer" (click)="toggleCategory(category.id)">
                       <div class="flex items-center text-gray-700">
                          <img [src]="category.image" class="w-6 h-6 mr-3 object-contain">
                          <span class="font-medium">{{ category.label }}</span>
                       </div>
                       <mat-icon class="text-gray-400 text-sm transition-transform duration-200" [class.rotate-180]="expandedCategory === category.id">expand_more</mat-icon>
                    </div>
                    
                    @if (expandedCategory === category.id) {
                       <div class="bg-gray-50 px-4 py-3 rounded-lg mt-1 mb-2 ml-8 space-y-4">
                          @for (subGroup of category.subcategories; track subGroup.title) {
                             <div>
                                <h4 class="text-xs font-bold text-gray-500 uppercase mb-2">{{ subGroup.title }}</h4>
                                <ul class="space-y-2 border-l-2 border-gray-200 pl-3">
                                   @for (item of subGroup.items; track item) {
                                      <li class="text-sm text-gray-600 py-1">{{ item }}</li>
                                   }
                                </ul>
                             </div>
                          }
                       </div>
                    }
                 </div>
              }
            </div>
          </div>
        </div>
      }
    </nav>
  `
})
export class NavbarComponent {
  mobileMenuOpen = false;
  expandedCategory: string | null = null;
  menuData = MENU_DATA;

  constructor(public authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
    this.mobileMenuOpen = false;
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  toggleCategory(categoryId: string): void {
    if (this.expandedCategory === categoryId) {
      this.expandedCategory = null;
    } else {
      this.expandedCategory = categoryId;
    }
  }
}
