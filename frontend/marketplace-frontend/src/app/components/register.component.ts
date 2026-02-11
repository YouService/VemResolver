import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { TipoUsuario } from '../models/models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterModule,
    MatCardModule, 
    MatFormFieldModule, 
    MatInputModule, 
    MatButtonModule,
    MatSelectModule,
    MatSnackBarModule
  ],
  template: `
    <div class="flex items-center justify-center min-h-screen bg-gray-100 py-8">
      <mat-card class="w-full max-w-md p-6">
        <h2 class="text-2xl font-bold mb-6 text-center">Cadastro</h2>
        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
          <mat-form-field class="w-full mb-4">
            <mat-label>Nome</mat-label>
            <input matInput formControlName="nome" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Senha</mat-label>
            <input matInput type="password" formControlName="senha" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Confirmar Senha</mat-label>
            <input matInput type="password" formControlName="confirmarSenha" required>
            @if (registerForm.get('confirmarSenha')?.errors?.['mismatch']) {
              <mat-error>As senhas não coincidem</mat-error>
            }
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Telefone</mat-label>
            <input matInput formControlName="telefone" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>CPF</mat-label>
            <input matInput formControlName="cpf" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>RG</mat-label>
            <input matInput formControlName="rg" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Data de Expedição</mat-label>
            <input matInput type="date" formControlName="dataExpedicao" required>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Tipo de Usuário</mat-label>
            <mat-select formControlName="tipo" required>
              <mat-option value="CLIENTE">Cliente</mat-option>
              <mat-option value="PRESTADOR">Prestador</mat-option>
            </mat-select>
          </mat-form-field>

          <button mat-raised-button color="primary" type="submit" class="w-full mb-4" 
                  [disabled]="registerForm.invalid || loading">
            {{ loading ? 'Cadastrando...' : 'Cadastrar' }}
          </button>

          <div class="text-center">
            <a routerLink="/login" class="text-blue-600">Já tem conta? Faça login</a>
          </div>
        </form>
      </mat-card>
    </div>
  `
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registerForm = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(6)]],
      confirmarSenha: ['', [Validators.required]],
      telefone: ['', [Validators.required]],
      cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      rg: ['', [Validators.required]],
      dataExpedicao: ['', [Validators.required]],
      tipo: ['CLIENTE', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const password = form.get('senha');
    const confirmPassword = form.get('confirmarSenha');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ mismatch: true });
      return { mismatch: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      this.authService.register(this.registerForm.value).subscribe({
        next: (response) => {
          this.snackBar.open('Cadastro realizado com sucesso!', 'Fechar', { duration: 3000 });
          if (response.tipoUsuario === TipoUsuario.CLIENTE) {
            this.router.navigate(['/cliente/dashboard']);
          } else {
            this.router.navigate(['/prestador/dashboard']);
          }
        },
        error: (error) => {
          this.snackBar.open('Erro ao cadastrar. Tente novamente.', 'Fechar', { duration: 3000 });
          this.loading = false;
        }
      });
    }
  }
}
