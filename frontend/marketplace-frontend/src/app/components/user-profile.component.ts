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
  templateUrl: './user-profile.component.html'
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
          dataExpedicao: user.dataExpedicaoRg,
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
