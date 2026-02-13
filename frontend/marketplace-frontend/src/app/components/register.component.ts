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
import { ViaCEPService } from '../services/viacep.service';
import { MaskDirective } from '../directives/mask.directive';
import { SeletorUfComponent } from './seletor-uf.component';
import { TipoUsuario } from '../models/models';
import { CustomValidators } from '../validators/custom.validators';

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
    MatSnackBarModule,
    MaskDirective,
    SeletorUfComponent
  ],
  template: `
    <div class="flex items-center justify-center min-h-screen bg-gray-100 py-8">
      <mat-card class="w-full max-w-md p-6">
        <h2 class="text-2xl font-bold mb-6 text-center">Cadastro</h2>
        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
          <mat-form-field class="w-full mb-4">
            <mat-label>Nome</mat-label>
            <input matInput formControlName="nome" required>
            <mat-error *ngIf="registerForm.get('nome')?.hasError('required')">Nome é obrigatório</mat-error>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Email</mat-label>
            <input matInput type="email" formControlName="email" required>
            <mat-error *ngIf="registerForm.get('email')?.hasError('required')">Email é obrigatório</mat-error>
            <mat-error *ngIf="registerForm.get('email')?.hasError('email')">Email inválido</mat-error>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Senha</mat-label>
            <input matInput type="password" formControlName="senha" required>
            <mat-error *ngIf="registerForm.get('senha')?.hasError('required')">Senha é obrigatória</mat-error>
            <mat-error *ngIf="registerForm.get('senha')?.hasError('minlength')">Senha deve ter no mínimo 8 caracteres</mat-error>
            <mat-error *ngIf="registerForm.get('senha')?.hasError('pattern')">Deve conter letras maiúsculas, minúsculas e números</mat-error>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Confirmar Senha</mat-label>
            <input matInput type="password" formControlName="confirmarSenha" required>
            <mat-error *ngIf="registerForm.get('confirmarSenha')?.hasError('required')">Confirmação de senha é obrigatória</mat-error>
            <mat-error *ngIf="registerForm.get('confirmarSenha')?.hasError('passwordMismatch')">As senhas não conferem</mat-error>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Telefone</mat-label>
            <input matInput formControlName="telefone" appMask="(00) 00000-0000" maxlength="15">
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>CPF</mat-label>
            <input matInput formControlName="cpf" appMask="000.000.000-00" maxlength="14">
            <mat-error *ngIf="registerForm.get('cpf')?.hasError('invalidCpf')">CPF inválido</mat-error>
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>RG</mat-label>
            <input matInput formControlName="rg">
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Órgão Expedidor</mat-label>
            <input matInput formControlName="orgaoExpedidor" placeholder="Ex: SSP/SP">
          </mat-form-field>

          <mat-form-field class="w-full mb-4">
            <mat-label>Data de Expedição</mat-label>
            <input matInput type="text" formControlName="dataExpedicaoRg" placeholder="DD/MM/AAAA" maxlength="10">
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
    private viaCEPService: ViaCEPService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.registerForm = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/)]],
      confirmarSenha: ['', [Validators.required]],
      telefone: [''],
      cpf: ['', [CustomValidators.cpf]],
      rg: [''],
      orgaoExpedidor: [''],
      dataExpedicaoRg: [''],
      tipo: ['CLIENTE', [Validators.required]]
    }, { validators: CustomValidators.passwordMatch('senha', 'confirmarSenha') });

    this.setupDataExpedicaoMask();
  }

  setupDataExpedicaoMask(): void {
    this.registerForm.get('dataExpedicaoRg')?.valueChanges.subscribe(value => {
      if (!value) return;

      const v = value.replace(/\D/g, '');

      if (v.length > 8) {
        this.registerForm.get('dataExpedicaoRg')?.setValue(v.substring(0, 8), { emitEvent: false });
        return;
      }

      let formatted = '';
      for (let i = 0; i < v.length; i++) {
        if (i === 2 || i === 4) {
          formatted += '/';
        }
        formatted += v[i];
      }

      this.registerForm.get('dataExpedicaoRg')?.setValue(formatted, { emitEvent: false });
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading = true;
      // Remove confirmarSenha antes de enviar
      const { confirmarSenha, ...dadosCadastro } = this.registerForm.value;
      
      this.authService.register(dadosCadastro).subscribe({
        next: (response) => {
          this.snackBar.open('Cadastro realizado com sucesso! Verifique seu email para ativar sua conta.', 'Entendi', { duration: 8000 });
          this.router.navigate(['/login']);
        },
        error: (error) => {
          const msg = error.error?.message || 'Erro ao cadastrar. Verifique os dados e tente novamente.';
          this.snackBar.open(msg, 'Fechar', { duration: 5000 });
          this.loading = false;
        }
      });
    }
  }
}
