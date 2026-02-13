import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-verify-account',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatButtonModule
  ],
  template: `
    <div class="flex items-center justify-center min-h-screen bg-gray-100 p-4">
      <mat-card class="max-w-md w-full p-8 text-center">
        @if (loading) {
          <div class="flex flex-col items-center">
            <mat-spinner diameter="48" class="mb-4"></mat-spinner>
            <h2 class="text-xl font-semibold text-gray-700">Verificando sua conta...</h2>
          </div>
        } @else if (success) {
          <div class="text-green-600 mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800 mb-2">Conta Verificada!</h2>
          <p class="text-gray-600 mb-6">Sua conta foi ativada com sucesso. Você já pode fazer login.</p>
          <a routerLink="/login" mat-raised-button color="primary" class="w-full">
            Ir para Login
          </a>
        } @else {
          <div class="text-red-600 mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-gray-800 mb-2">Falha na Verificação</h2>
          <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
          <a routerLink="/login" mat-stroked-button color="primary" class="w-full">
            Voltar para Login
          </a>
        }
      </mat-card>
    </div>
  `
})
export class VerifyAccountComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    
    if (!token) {
      this.loading = false;
      this.success = false;
      this.errorMessage = 'Token de verificação não encontrado.';
      return;
    }

    this.http.get(`${environment.apiUrl}/auth/verify?token=${token}`, { responseType: 'text' })
      .subscribe({
        next: () => {
          this.loading = false;
          this.success = true;
        },
        error: (error) => {
          this.loading = false;
          this.success = false;
          // Tenta extrair a mensagem de erro do backend
          this.errorMessage = error.error?.message || error.error || 'O link de verificação é inválido ou expirou.';
          // Se o backend retornar texto puro como erro
          if (typeof error.error === 'string' && error.error.includes('{')) {
             try {
                const parsed = JSON.parse(error.error);
                if (parsed.message) this.errorMessage = parsed.message;
             } catch (e) {}
          }
        }
      });
  }
}