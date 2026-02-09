import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatSnackBarModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-8">
      <div class="container mx-auto px-4 max-w-2xl">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900">Meu Perfil</h1>
          <p class="text-gray-600 mt-2">Gerencie suas informações pessoais</p>
        </div>

        <!-- Form Card -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <form [formGroup]="profileForm" (ngSubmit)="salvarPerfil()">
            
            <!-- Nome -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                Nome
                <span class="text-red-500">*</span>
              </label>
              <input
                type="text"
                formControlName="nome"
                placeholder="Seu nome completo"
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                [class.border-red-500]="profileForm.get('nome')?.invalid && profileForm.get('nome')?.touched">
              @if (profileForm.get('nome')?.invalid && profileForm.get('nome')?.touched) {
                <p class="text-red-500 text-sm mt-1">Nome é obrigatório</p>
              }
            </div>

            <!-- Email (Readonly) -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                Email
              </label>
              <input
                type="email"
                formControlName="email"
                class="w-full px-4 py-3 border rounded-lg bg-gray-100"
                readonly>
            </div>

            <!-- CPF (Readonly) -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                CPF (não pode ser alterado)
              </label>
              <input
                type="text"
                formControlName="cpf"
                class="w-full px-4 py-3 border rounded-lg bg-gray-100"
                readonly>
            </div>

            <!-- RG (Readonly) -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                RG (não pode ser alterado)
              </label>
              <input
                type="text"
                formControlName="rg"
                class="w-full px-4 py-3 border rounded-lg bg-gray-100"
                readonly>
            </div>

            <!-- Data de Expedição (Readonly) -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                Data de Expedição (não pode ser alterada)
              </label>
              <input
                type="date"
                formControlName="dataExpedicao"
                class="w-full px-4 py-3 border rounded-lg bg-gray-100"
                readonly>
            </div>

            <!-- Telefone -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                Telefone
                <span class="text-red-500">*</span>
              </label>
              <input
                type="tel"
                formControlName="telefone"
                placeholder="(XX) XXXXX-XXXX"
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                [class.border-red-500]="profileForm.get('telefone')?.invalid && profileForm.get('telefone')?.touched">
              @if (profileForm.get('telefone')?.invalid && profileForm.get('telefone')?.touched) {
                <p class="text-red-500 text-sm mt-1">Telefone é obrigatório</p>
              }
            </div>

            <!-- Nova Senha (Opcional) -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                Nova Senha (deixe em branco para manter)
              </label>
              <input
                type="password"
                formControlName="novaSenha"
                placeholder="Mínimo 6 caracteres"
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
              @if (profileForm.get('novaSenha')?.invalid && profileForm.get('novaSenha')?.touched) {
                <p class="text-red-500 text-sm mt-1">Senha deve ter no mínimo 6 caracteres</p>
              }
            </div>

            <!-- Confirmar Senha -->
            <div class="mb-4">
              <label class="block text-gray-700 font-semibold mb-2">
                Confirmar Nova Senha
              </label>
              <input
                type="password"
                formControlName="confirmarSenha"
                placeholder="Digite novamente a nova senha"
                class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
              @if (profileForm.get('confirmarSenha')?.errors?.['mismatch']) {
                <p class="text-red-500 text-sm mt-1">As senhas não coincidem</p>
              }
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
                (click)="voltar()"
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
                  <span>Salvar Alterações</span>
                }
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class UserProfileComponent implements OnInit {
  profileForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      cpf: ['', [Validators.required]],
      rg: ['', [Validators.required]],
      dataExpedicao: ['', [Validators.required]],
      telefone: ['', [Validators.required]],
      novaSenha: ['', [Validators.minLength(6)]],
      confirmarSenha: ['']
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const novaSenha = form.get('novaSenha');
    const confirmarSenha = form.get('confirmarSenha');

    if (novaSenha && novaSenha.value && confirmarSenha && confirmarSenha.value && 
        novaSenha.value !== confirmarSenha.value) {
      confirmarSenha.setErrors({ mismatch: true });
      return { mismatch: true };
    }
    return null;
  }

  ngOnInit() {
    this.carregarPerfil();
  }

  carregarPerfil() {
    const userId = this.authService.getCurrentUserId();
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    this.userService.buscarPorId(userId).subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          nome: user.nome,
          email: user.email,
          cpf: user.cpf,
          rg: user.rg,
          dataExpedicao: user.dataExpedicao,
          telefone: user.telefone
        });
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar perfil';
      }
    });
  }

  salvarPerfil() {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.profileForm.invalid) {
      Object.keys(this.profileForm.controls).forEach(key => {
        this.profileForm.get(key)?.markAsTouched();
      });
      this.errorMessage = 'Por favor, preencha todos os campos obrigatórios';
      return;
    }

    this.loading = true;
    const userId = this.authService.getCurrentUserId();
    
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    const updateData = {
      nome: this.profileForm.value.nome,
      telefone: this.profileForm.value.telefone,
      senha: this.profileForm.value.novaSenha || null
    };

    this.userService.atualizar(userId, updateData).subscribe({
      next: () => {
        this.successMessage = 'Perfil atualizado com sucesso!';
        this.loading = false;
        this.snackBar.open('Perfil atualizado com sucesso!', 'Fechar', { duration: 3000 });
        
        if (this.authService.getCurrentUser()?.tipo === 'CLIENTE') {
          this.router.navigate(['/cliente/dashboard']);
        } else {
          this.router.navigate(['/prestador/dashboard']);
        }
      },
      error: (error) => {
        this.errorMessage = 'Erro ao atualizar perfil. Tente novamente.';
        this.loading = false;
      }
    });
  }

  voltar() {
    if (this.authService.getCurrentUser()?.tipo === 'CLIENTE') {
      this.router.navigate(['/cliente/dashboard']);
    } else {
      this.router.navigate(['/prestador/dashboard']);
    }
  }
}
