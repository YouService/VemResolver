import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { Router, RouterModule } from '@angular/router';
import { MaskDirective } from '../../directives/mask.directive';
import { UppercaseDirective } from '../../directives/uppercase.directive';
import { AuthService } from '../../services/auth.service';
import { ViaCEPService } from '../../services/viacep.service';
import { CustomValidators } from '../../validators/custom.validators';
import { SnackService } from '../../services/snack.service';

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
    MatCheckboxModule,
    MatStepperModule,
    MatIconModule,
    MatSnackBarModule,
    MaskDirective,
    UppercaseDirective
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  step1Form: FormGroup;
  step2Form: FormGroup;
  step3Form: FormGroup;
  loading = false;
  loadingCep = false;
  mobileMenuOpen = false;

  // Mensagens de erro para exibição no template
  step1Error = '';
  step2Error = '';
  step3Error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private viaCEPService: ViaCEPService,
    private router: Router,
    private snackService: SnackService
  ) {
    this.step1Form = this.fb.group({
      cpf: ['', [Validators.required, CustomValidators.cpf]],
      termsAccepted: [false, [Validators.requiredTrue]]
    });

    this.step2Form = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$/)]],
      confirmarSenha: ['', [Validators.required]],
      telefone: ['', [Validators.required]],
      rg: [''],
      orgaoExpedidor: [''],
      dataExpedicaoRg: [''],
      tipo: ['CLIENTE', [Validators.required]]
    }, { validators: CustomValidators.passwordMatch('senha', 'confirmarSenha') });

    this.step3Form = this.fb.group({
      cep: ['', [Validators.required]],
      endereco: ['', [Validators.required]],
      cidade: ['', [Validators.required]],
      estado: ['', [Validators.required]],
      numero: [''],
      complemento: ['']
    });

    this.setupDataExpedicaoMask();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
    this.mobileMenuOpen = false;
  }

  setupDataExpedicaoMask(): void {
    this.step2Form.get('dataExpedicaoRg')?.valueChanges.subscribe(value => {
      if (!value) return;
      const v = value.replace(/\D/g, '');
      if (v.length > 8) {
        this.step2Form.get('dataExpedicaoRg')?.setValue(v.substring(0, 8), { emitEvent: false });
        return;
      }
      let formatted = '';
      for (let i = 0; i < v.length; i++) {
        if (i === 2 || i === 4) formatted += '/';
        formatted += v[i];
      }
      this.step2Form.get('dataExpedicaoRg')?.setValue(formatted, { emitEvent: false });
    });
  }

  buscarCep(): void {
    const cep = this.step3Form.get('cep')?.value?.replace(/\D/g, '');
    if (cep && cep.length === 8) {
      this.loadingCep = true;
      this.step3Error = '';
      this.viaCEPService.buscarCEP(cep).subscribe({
        next: (data) => {
          if (data.erro) {
            this.step3Error = 'CEP não encontrado. Preencha manualmente.';
            this.snackService.open(this.step3Error, 'Erro CEP', 'error');
            this.habilitarCamposEndereco();
          } else {
            this.step3Form.patchValue({
              endereco: data.logradouro,
              cidade: data.localidade,
              estado: data.uf
            });
            this.desabilitarCamposEndereco();
          }
          this.loadingCep = false;
        },
        error: () => {
          this.step3Error = 'Erro ao buscar CEP. Preencha manualmente.';
          this.snackService.open(this.step3Error, 'Erro CEP', 'error');
          this.habilitarCamposEndereco();
          this.loadingCep = false;
        }
      });
    }
  }

  habilitarCamposEndereco(): void {
    this.step3Form.get('endereco')?.enable();
    this.step3Form.get('cidade')?.enable();
    this.step3Form.get('estado')?.enable();
  }

  desabilitarCamposEndereco(): void {
    this.step3Form.get('endereco')?.disable();
    this.step3Form.get('cidade')?.disable();
    this.step3Form.get('estado')?.disable();
  }

  onSubmit(): void {
    if (this.step1Form.valid && this.step2Form.valid && this.step3Form.valid) {
      this.loading = true;
      this.step1Error = '';
      this.step2Error = '';
      this.step3Error = '';

      const dadosCadastro = {
        ...this.step1Form.getRawValue(),
        ...this.step2Form.value,
        ...this.step3Form.getRawValue()
      };

      delete dadosCadastro.confirmarSenha;
      delete dadosCadastro.termsAccepted;

      this.authService.register(dadosCadastro).subscribe({
        next: (response) => {
          this.loading = false;
          // Feedback visual positivo para o usuário
          this.snackService.open('✓ Cadastro realizado! Verifique seu email para ativar a conta.', 'Sucesso', 'success', 8000);
          
          // Reset dos formulários
          this.step1Form.reset();
          this.step2Form.reset();
          this.step3Form.reset();

          // Redirecionamento após um pequeno delay para o usuário ler a snackbar
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (error) => {
          this.loading = false;
          // Se o backend enviar uma mensagem de negócio (ex: email duplicado), exibimos ela
          const msg = error.error?.message || 'Erro interno ao processar cadastro. Tente novamente em instantes.';
          this.step2Error = msg;
          this.snackService.open('✕ ' + msg, 'Erro', 'error', 6000);
        }
      });
    } else {
      this.snackService.open('Por favor, preencha todos os campos obrigatórios corretamente.', 'Aviso', 'warning');
    }
  }
}