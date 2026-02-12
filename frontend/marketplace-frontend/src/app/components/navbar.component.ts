import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="bg-white shadow-md sticky top-0 z-50">
      <div class="container mx-auto px-4">
        <div class="flex items-center justify-between h-16">
          <!-- Logo -->
          <div class="flex items-center">
            <a routerLink="/" class="text-2xl font-bold text-blue-600 hover:text-blue-700 transition">
              🛠️ VemResolver
            </a>
          </div>

          <!-- Navigation Links -->
          <div class="hidden md:flex items-center space-x-6">
            <a routerLink="/" class="text-gray-700 hover:text-blue-600 transition font-medium">
              Início
            </a>
            <a href="#como-funciona" class="text-gray-700 hover:text-blue-600 transition font-medium">
              Como Funciona
            </a>
            <a href="#categorias" class="text-gray-700 hover:text-blue-600 transition font-medium">
              Categorias
            </a>
          </div>

          <!-- User Menu -->
          <div class="flex items-center space-x-4">
            @if (authService.currentUser$ | async; as user) {
              <div class="flex items-center space-x-4">
                <span class="text-gray-700">
                  <span class="hidden md:inline">Olá, </span>
                  <strong>{{ user.nome }}</strong>
                </span>

                @if (authService.isCliente()) {
                  <a
                    routerLink="/cliente/dashboard"
                    class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium">
                    Meu Painel
                  </a>
                  <a
                    routerLink="/cliente/perfil"
                    class="text-gray-700 hover:text-blue-600 transition font-medium">
                    Perfil
                  </a>
                }

                @if (authService.isPrestador()) {
                  <a
                    routerLink="/prestador/dashboard"
                    class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium">
                    Meu Painel
                  </a>
                  <a
                    routerLink="/prestador/perfil"
                    class="text-gray-700 hover:text-blue-600 transition font-medium">
                    Perfil
                  </a>
                }

                <button
                  (click)="logout()"
                  class="text-gray-700 hover:text-red-600 transition font-medium">
                  Sair
                </button>
              </div>
            } @else {
              <a
                routerLink="/login"
                class="text-gray-700 hover:text-blue-600 transition font-medium">
                Entrar
              </a>
              <a
                routerLink="/register"
                class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition font-medium">
                Cadastrar
              </a>
            }
          </div>

          <!-- Mobile Menu Button -->
          <button
            (click)="toggleMobileMenu()"
            class="md:hidden text-gray-700 hover:text-blue-600 transition">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>

        <!-- Mobile Menu -->
        @if (mobileMenuOpen) {
          <div class="md:hidden py-4 border-t">
            <div class="flex flex-col space-y-3">
              <a routerLink="/" class="text-gray-700 hover:text-blue-600 transition font-medium py-2">
                Início
              </a>
              <a href="#como-funciona" class="text-gray-700 hover:text-blue-600 transition font-medium py-2">
                Como Funciona
              </a>
              <a href="#categorias" class="text-gray-700 hover:text-blue-600 transition font-medium py-2">
                Categorias
              </a>

              @if (!(authService.currentUser$ | async)) {
                <a routerLink="/login" class="text-gray-700 hover:text-blue-600 transition font-medium py-2">
                  Entrar
                </a>
                <a routerLink="/register" class="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition font-medium text-center">
                  Cadastrar
                </a>
              }
            </div>
          </div>
        }
      </div>
    </nav>
  `
})
export class NavbarComponent {
  mobileMenuOpen = false;

  constructor(public authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }
}
