import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PropostaService } from '../services/proposta.service';
import { SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-prestador-enviar-proposta',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './prestador-enviar-proposta.component.html'
})
export class PrestadorEnviarPropostaComponent implements OnInit {
  solicitacao?: SolicitacaoServico;
  propostaForm: FormGroup;

  loading = true;
  enviando = false;
  sucesso = false;
  erroCarregamento = '';
  erroEnvio = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private solicitacaoService: SolicitacaoService,
    private propostaService: PropostaService
  ) {
    this.propostaForm = this.fb.group({
      valor: [null, [Validators.required, Validators.min(1)]],
      mensagem: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (s) => {
        this.solicitacao = s;
        this.loading = false;
      },
      error: () => {
        this.erroCarregamento = 'Solicitação não encontrada ou não está mais disponível.';
        this.loading = false;
      }
    });
  }

  enviar(): void {
    if (this.propostaForm.invalid) {
      Object.keys(this.propostaForm.controls).forEach(key =>
        this.propostaForm.get(key)?.markAsTouched()
      );
      return;
    }

    this.enviando = true;
    this.erroEnvio = '';

    this.propostaService.criar({
      solicitacaoId: this.solicitacao!.id!,
      valor: this.propostaForm.value.valor,
      mensagem: this.propostaForm.value.mensagem
    }).subscribe({
      next: () => {
        this.sucesso = true;
        this.enviando = false;
      },
      error: (err) => {
        this.enviando = false;
        this.erroEnvio = err.error?.message || 'Erro ao enviar proposta. Tente novamente.';
      }
    });
  }

  voltar(): void {
    this.router.navigate(['/prestador/dashboard']);
  }

  formatarData(data: any): string {
    if (!data) return '';
    const date = new Date(data);
    const hoje = new Date();
    const diff = hoje.getTime() - date.getTime();
    const dias = Math.floor(diff / (1000 * 60 * 60 * 24));
    if (dias === 0) return 'Hoje';
    if (dias === 1) return 'Ontem';
    if (dias < 7) return `${dias} dias atrás`;
    return date.toLocaleDateString('pt-BR');
  }
}
