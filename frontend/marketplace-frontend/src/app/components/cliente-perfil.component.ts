import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ViaCEPService } from '../services/viacep.service';
import { MaskDirective } from '../directives/mask.directive';
import { SeletorUfComponent } from './seletor-uf.component';
import { User, UpdatePerfilRequest, AlterarSenhaRequest } from '../models/models';

@Component({
  selector: 'app-cliente-perfil',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ReactiveFormsModule, MaskDirective, SeletorUfComponent],
  templateUrl: './cliente-perfil.component.html'
})
export class ClientePerfilComponent implements OnInit {
  abaAtiva: 'dados' | 'senha' | 'avaliacoes' | 'excluir' = 'dados';
  loading = false;
  salvando = false;
  alterandoSenha = false;
  excluindoConta = false;
  successMessage = '';
  errorMessage = '';
  successMessageSenha = '';
  errorMessageSenha = '';
  errorMessageExclusao = '';
  confirmarExclusao = false;
  user: User | null = null;

  perfilForm: FormGroup;
  senhaForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private viaCEPService: ViaCEPService,
    private router: Router
  ) {
    this.perfilForm = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      telefone: [''],
      cpf: [''],
      rg: [''],
      orgaoExpedidor: [''],
      dataExpedicaoRg: [''],
      cep: [''],
      endereco: [''],
      cidade: [''],
      estado: ['']
    });

    this.senhaForm = this.fb.group({
      senhaAtual: ['', [Validators.required]],
      novaSenha: ['', [Validators.required, Validators.minLength(6)]],
      confirmarSenha: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.carregarPerfil();
    this.setupDataExpedicaoMask();
  }

  setupDataExpedicaoMask(): void {
    this.perfilForm.get('dataExpedicaoRg')?.valueChanges.subscribe(value => {
      if (!value) return;

      const v = value.replace(/\D/g, '');

      if (v.length > 8) {
        this.perfilForm.get('dataExpedicaoRg')?.setValue(v.substring(0, 8), { emitEvent: false });
        return;
      }

      let formatted = '';
      for (let i = 0; i < v.length; i++) {
        if (i === 2 || i === 4) {
          formatted += '/';
        }
        formatted += v[i];
      }

      this.perfilForm.get('dataExpedicaoRg')?.setValue(formatted, { emitEvent: false });
    });
  }

  carregarPerfil() {
    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.authService.buscarPerfil().subscribe({
      next: (user) => {
        this.user = user;
        this.perfilForm.patchValue({
          nome: user.nome,
          email: user.email,
          telefone: user.telefone || '',
          cpf: user.cpf || '',
          rg: user.rg || '',
          orgaoExpedidor: user.orgaoExpedidor || '',
          dataExpedicaoRg: user.dataExpedicaoRg || '',
          cep: user.cep || '',
          endereco: user.endereco || '',
          cidade: user.cidade || '',
          estado: user.estado || ''
        });
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar perfil', error);
        this.errorMessage = 'Erro ao carregar perfil';
        this.loading = false;
      }
    });
  }

  salvarPerfil() {
    if (this.perfilForm.invalid) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    this.salvando = true;
    this.successMessage = '';
    this.errorMessage = '';

    const dados: UpdatePerfilRequest = this.perfilForm.value;

    this.authService.atualizarPerfil(dados).subscribe({
      next: () => {
        this.successMessage = 'Perfil atualizado com sucesso!';
        this.salvando = false;
        setTimeout(() => {
          this.successMessage = '';
          this.router.navigate(['/cliente/dashboard']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('Erro ao atualizar perfil', error);
        this.errorMessage = error.error?.message || 'Erro ao atualizar perfil';
        this.salvando = false;
      }
    });
  }

  alterarSenha() {
    if (this.senhaForm.invalid) {
      this.senhaForm.markAllAsTouched();
      return;
    }

    const senha = this.senhaForm.value;
    if (senha.novaSenha !== senha.confirmarSenha) {
      this.errorMessageSenha = 'Nova senha e confirmação não conferem';
      return;
    }

    this.alterandoSenha = true;
    this.successMessageSenha = '';
    this.errorMessageSenha = '';

    const dados: AlterarSenhaRequest = this.senhaForm.value;

    this.authService.alterarSenha(dados).subscribe({
      next: () => {
        this.successMessageSenha = 'Senha alterada com sucesso!';
        this.senhaForm.reset();
        this.alterandoSenha = false;
        setTimeout(() => {
          this.successMessageSenha = '';
          this.router.navigate(['/cliente/dashboard']);
        }, 1500);
      },
      error: (error: any) => {
        console.error('Erro ao alterar senha', error);
        this.errorMessageSenha = error.error?.message || 'Erro ao alterar senha';
        this.alterandoSenha = false;
      }
    });
  }

  voltarParaDashboard() {
    this.router.navigate(['/cliente/dashboard']);
  }

  buscarEnderecoPorCEP() {
    const cep = this.perfilForm.get('cep')?.value;
    if (!cep || cep.replace(/\D/g, '').length !== 8) {
      return;
    }

    this.viaCEPService.buscarCEP(cep).subscribe({
      next: (endereco) => {
        if (endereco.erro) {
          this.errorMessage = 'CEP não encontrado. Verifique o CEP digitado.';
          return;
        }
        this.errorMessage = '';
        this.perfilForm.patchValue({
          endereco: endereco.logradouro || '',
          cidade: endereco.localidade || '',
          estado: endereco.uf || ''
        });
      },
      error: () => {
        this.errorMessage = 'Erro ao buscar o CEP. Verifique sua conexão e tente novamente.';
      }
    });
  }

  deletarConta() {
    this.excluindoConta = true;
    this.errorMessageExclusao = '';

    this.authService.deletarConta().subscribe({
      next: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('userTipo');
        this.router.navigate(['/login']);
      },
      error: (error: any) => {
        console.error('Erro ao excluir conta', error);
        this.errorMessageExclusao = error.error?.message || 'Erro ao excluir conta';
        this.excluindoConta = false;
        this.confirmarExclusao = false;
      }
    });
  }
}
