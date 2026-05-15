import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AvaliacaoService } from '../services/avaliacao.service';
import { SolicitacaoService } from '../services/solicitacao.service';
import { SolicitacaoServico } from '../models/models';

@Component({
  selector: 'app-prestador-avaliacao',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './prestador-avaliacao.component.html'
})
export class PrestadorAvaliacaoComponent implements OnInit {
  solicitacao: SolicitacaoServico | null = null;

  nota = 0;
  notaHover = 0;
  comentario = '';

  loading = true;
  enviando = false;
  enviado = false;
  jaAvaliado = false;
  notaEnviada = 0;
  tentouEnviar = false;
  erroCarregamento = '';
  erroEnvio = '';

  constructor(
    private route: ActivatedRoute,
    private solicitacaoService: SolicitacaoService,
    private avaliacaoService: AvaliacaoService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitacaoService.buscarPorId(id).subscribe({
      next: (sol) => {
        if (sol.status !== 'CONCLUIDA') {
          this.erroCarregamento = 'Este serviço ainda não foi concluído e não pode ser avaliado.';
          this.loading = false;
          return;
        }
        if (sol.foiAvaliadoPeloPrestador) {
          this.jaAvaliado = true;
          this.loading = false;
          return;
        }
        this.solicitacao = sol;
        this.loading = false;
      },
      error: () => {
        this.erroCarregamento = 'Solicitação não encontrada.';
        this.loading = false;
      }
    });
  }

  get notaTexto(): string {
    const textos = ['', 'Péssimo', 'Ruim', 'Regular', 'Bom', 'Excelente'];
    return textos[this.nota] || '';
  }

  get notaClass(): string {
    const classes = ['', 'text-red-600', 'text-orange-500', 'text-yellow-600', 'text-blue-600', 'text-green-600'];
    return classes[this.nota] || '';
  }

  enviar(): void {
    this.tentouEnviar = true;
    this.erroEnvio = '';

    if (this.nota === 0) return;
    if (!this.solicitacao?.id) return;

    this.enviando = true;
    this.avaliacaoService.criarParaCliente({
      solicitacaoId: this.solicitacao.id,
      nota: this.nota,
      comentario: this.comentario || undefined
    } as any).subscribe({
      next: () => {
        this.notaEnviada = this.nota;
        this.enviado = true;
        this.enviando = false;
      },
      error: (err) => {
        this.enviando = false;
        const msg = err?.error?.message || err?.error?.erro;
        if (msg?.includes('já avaliou')) {
          this.erroEnvio = 'Você já avaliou o cliente desta solicitação.';
        } else if (msg?.includes('não prestou')) {
          this.erroEnvio = 'Você não prestou este serviço.';
        } else {
          this.erroEnvio = 'Erro ao enviar avaliação. Tente novamente.';
        }
      }
    });
  }
}
