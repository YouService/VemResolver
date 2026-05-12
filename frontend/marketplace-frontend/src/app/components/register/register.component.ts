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
import { NotificationService } from '../../services/notification.service';
import { ViaCEPService } from '../../services/viacep.service';
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
  step1Form!: FormGroup;
  step2Form!: FormGroup;
  step3Form!: FormGroup;
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
    private notificationService: NotificationService
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
            this.step3Error = 'CEP não encontrado. Preencha os campos manualmente.';
            this.notificationService.error(this.step3Error, 'Erro ao buscar CEP');
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
          this.step3Error = 'Erro ao buscar CEP. Preencha os campos manualmente.';
          this.notificationService.error(this.step3Error, 'Erro ao buscar CEP');
          this.habilitarCamposEndereco();
          this.loadingCep = false;
        }
      });
    }
  }

  codificar(valor: string): string {
    return btoa(valor);
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
    // Validar todos os steps
    if (!this.step1Form.valid || !this.step2Form.valid || !this.step3Form.valid) {
      this.notificationService.warning(
        'Atenção',
        'Preencha todos os campos obrigatórios.'
      );
      return;
    }

    if (this.step1Form.valid && this.step2Form.valid && this.step3Form.valid) {
      this.loading = true;
      this.clearErrors();

      // Criar payload
      const payload = this.createPayload();

      // Enviar para API
      this.authService.register(payload).subscribe({
        next: (response) => this.handleSuccess(response, payload),
        error: (error) => this.handleError(error, payload)
      });
    }
  }

  private clearAllForms(): void {
    this.step1Form.reset();
    this.step2Form.reset();
    this.step3Form.reset();
    this.clearErrors();
    this.loading = false;
  }

  private clearErrors(): void {
    this.step1Error = '';
    this.step2Error = '';
    this.step3Error = '';
  }

  private createPayload(): any {
    const payload = {
      ...this.step1Form.getRawValue(),
      ...this.step2Form.value,
      ...this.step3Form.getRawValue()
    };

    const senhaCodificada = this.codificar(this.step2Form.value.senha);

    // Remover campos desnecessários
    delete payload.confirmarSenha;
    delete payload.termsAccepted;
    payload.senha = senhaCodificada;

    return payload;
  }

  private handleSuccess(response: any, payload: any): void {
    this.clearAllForms();
    delete payload.senha;

    this.notificationService.success(
      'Cadastro realizado com sucesso!',
      'Verifique seu email para ativar sua conta.'
    );

    // Logout e redirecionar
    this.logout();
    setTimeout(() => {
      this.router.navigate(['/login']);
    }, 100);
  }

  private handleError(error: any, payload: any): void {
    delete payload.senha;
    this.step2Form.patchValue({
      senha: '',
      confirmarSenha: ''
    });

    // Tratar erro
    const msg = error.error?.message ||
                'Erro ao cadastrar. Tente novamente.';

    this.step2Error = msg;
    this.notificationService.error('Erro no cadastro', msg);
    this.loading = false;
  }
}
