import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  template: `
    <div class="flex items-center justify-center min-h-screen bg-gray-100">
      <mat-card class="w-full max-w-md p-6">
        <h2 class="text-2xl font-bold mb-6 text-center">Login</h2>
        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
          <mat-form-field class="w-full mb-4">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Senha</mat-label>
            <input matInput type="password" formControlName="senha" required>
          </mat-form-field>

          <button mat-raised-button color="primary" type="submit" class="w-full mb-4"
                  [disabled]="loginForm.invalid || loading">
            {{ loading ? 'Entrando...' : 'Entrar' }}
          </button>

          <div class="text-center mb-6">
            <a routerLink="/register" class="text-blue-600">Não tem conta? Cadastre-se</a>
          </div>

          <div class="mt-4 pt-6 border-t border-gray-100 text-center">
            <p class="text-sm text-gray-600 mb-2">Você é um profissional?</p>
            <a routerLink="/seja-um-profissional" class="text-blue-600 font-bold hover:underline">
              Cadastre-se para conseguir clientes
            </a>
          </div>
        </form>
      </mat-card>
    </div>
  `
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notifService: NotificationService,
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.notifService.warning('Por favor, preencha todos os campos corretamente.');
      return;
    }

    if (this.loginForm.valid) {
      this.loading = true;
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.notifService.success('Bem-vindo de volta!', 'Login realizado com sucesso!');
          if (response.tipoUsuario === 'CLIENTE') {
            this.router.navigate(['/cliente/dashboard']);
          } else {
            this.router.navigate(['/prestador/dashboard']);
          }
        },
        error: (error) => {
          const msg = error.error?.message || 'Erro ao fazer login. Verifique suas credenciais.';
          this.notifService.error(msg, 'Falha no Login');
          this.loading = false;
        }
      });
    }
  }
}
