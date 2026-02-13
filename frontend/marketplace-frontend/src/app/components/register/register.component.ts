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
import { AuthService } from '../../services/auth.service';
import { ViaCEPService } from '../../services/viacep.service';
import { MaskDirective } from '../../directives/mask.directive';
import { SeletorUfComponent } from '../seletor-uf.component';
import { TipoUsuario } from '../../models/models';
import { CustomValidators } from '../../validators/custom.validators';

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
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
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
