import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { ViaCEPService } from '../../services/viacep.service';
import { MaskDirective } from '../../directives/mask.directive';
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
    MaskDirective
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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private viaCEPService: ViaCEPService,
    private router: Router,
    private snackBar: MatSnackBar
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
        if (i === 2 || i === 4) {
          formatted += '/';
        }
        formatted += v[i];
      }

      this.step2Form.get('dataExpedicaoRg')?.setValue(formatted, { emitEvent: false });
    });
  }

  buscarCep(): void {
    const cep = this.step3Form.get('cep')?.value?.replace(/\D/g, '');
    
    if (cep && cep.length === 8) {
      this.loadingCep = true;
      this.viaCEPService.buscarCEP(cep).subscribe({
        next: (data) => {
          if (data.erro) {
            this.snackBar.open('CEP não encontrado. Preencha os campos manualmente.', 'Fechar', { duration: 3000 });
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
          this.snackBar.open('Erro ao buscar CEP. Preencha os campos manualmente.', 'Fechar', { duration: 3000 });
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

      const dadosCadastro = {
        ...this.step1Form.getRawValue(),
        ...this.step2Form.value,
        ...this.step3Form.getRawValue()
      };

      delete dadosCadastro.confirmarSenha;
      delete dadosCadastro.termsAccepted;

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
