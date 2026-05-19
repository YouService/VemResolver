import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AvaliacaoService } from '../services/avaliacao.service';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PropostaService } from '../services/proposta.service';
import { SolicitacaoServico, Proposta, StatusProposta } from '../models/models';

export interface DimensaoRating {
  label: string;
  field: 'notaPontualidade' | 'notaQualidade' | 'notaComunicacao';
  nota: number;
  hover: number;
}

@Component({
  selector: 'app-cliente-avaliacao',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './cliente-avaliacao.component.html'
})
export class ClienteAvaliacaoComponent implements OnInit {
  solicitacao: SolicitacaoServico | null = null;
  prestadorNome = '';
  prestadorId: number | null = null;

  nota = 0;
  notaHover = 0;
  comentario = '';

  dimensoes: DimensaoRating[] = [
    { label: 'Pontualidade', field: 'notaPontualidade', nota: 0, hover: 0 },
    { label: 'Qualidade do serviço', field: 'notaQualidade', nota: 0, hover: 0 },
    { label: 'Comunicação', field: 'notaComunicacao', nota: 0, hover: 0 }
  ];

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
    private router: Router,
    private solicitacaoService: SolicitacaoService,
    private propostaService: PropostaService,
    private avaliacaoService: AvaliacaoService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregarDados(id);
  }

  get notaTexto(): string {
    const textos = ['', 'Péssimo', 'Ruim', 'Regular', 'Bom', 'Excelente'];
    return textos[this.nota] || '';
  }

  get notaClass(): string {
    const classes = ['', 'text-red-600', 'text-orange-500', 'text-yellow-600', 'text-blue-600', 'text-green-600'];
    return classes[this.nota] || '';
  }

  textoNota(nota: number): string {
    const textos = ['', 'Péssimo', 'Ruim', 'Regular', 'Bom', 'Excelente'];
    return textos[nota] || '';
  }

  classNota(nota: number): string {
    const classes = ['', 'text-red-600', 'text-orange-500', 'text-yellow-600', 'text-blue-600', 'text-green-600'];
    return classes[nota] || '';
  }

  estrelaAtiva(dimensao: DimensaoRating, star: number): boolean {
    return (dimensao.hover || dimensao.nota) >= star;
  }

  private carregarDados(solicitacaoId: number): void {
    this.solicitacaoService.buscarPorId(solicitacaoId).subscribe({
      next: (sol) => {
        if (sol.status !== 'CONCLUIDA') {
          this.erroCarregamento = 'Este serviço ainda não foi concluído e não pode ser avaliado.';
          this.loading = false;
          return;
        }
        if (sol.foiAvaliadoPeloCliente) {
          this.jaAvaliado = true;
          this.loading = false;
          return;
        }
        this.solicitacao = sol;
        this.carregarPrestador(solicitacaoId);
      },
      error: () => {
        this.erroCarregamento = 'Solicitação não encontrada.';
        this.loading = false;
      }
    });
  }

  private carregarPrestador(solicitacaoId: number): void {
    this.propostaService.listarPorSolicitacao(solicitacaoId, 0, 20).subscribe({
      next: (response) => {
        const aceita = response.content.find(p => p.status === StatusProposta.ACEITA);
        if (aceita) {
          this.prestadorId = aceita.prestadorId ?? null;
          this.prestadorNome = aceita.prestadorNome ?? '';
        }
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  enviar(): void {
    this.tentouEnviar = true;
    this.erroEnvio = '';

    if (this.nota === 0) return;
    if (!this.solicitacao?.id || !this.prestadorId) return;

    const payload: any = {
      solicitacaoId: this.solicitacao.id,
      prestadorId: this.prestadorId,
      nota: this.nota,
      comentario: this.comentario || undefined
    };

    for (const dim of this.dimensoes) {
      if (dim.nota > 0) {
        payload[dim.field] = dim.nota;
      }
    }

    this.enviando = true;
    this.avaliacaoService.criar(payload).subscribe({
      next: () => {
        this.notaEnviada = this.nota;
        this.enviado = true;
        this.enviando = false;
      },
      error: (err) => {
        this.enviando = false;
        const msg = err?.error?.message || err?.error?.erro;
        if (msg?.includes('já foi avaliada')) {
          this.erroEnvio = 'Esta solicitação já foi avaliada anteriormente.';
        } else {
          this.erroEnvio = 'Erro ao enviar avaliação. Tente novamente.';
        }
      }
    });
  }
}
