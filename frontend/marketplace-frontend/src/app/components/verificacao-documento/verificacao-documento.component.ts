import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VerificacaoService } from '../../services/verificacao.service';
import { StatusVerificacao, VerificacaoDocumentoDTO } from '../../models/models';

@Component({
  selector: 'app-verificacao-documento',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verificacao-documento.component.html'
})
export class VerificacaoDocumentoComponent implements OnInit {
  verificacao?: VerificacaoDocumentoDTO | null;
  loading = true;
  enviando = false;
  erro = '';
  sucesso = '';

  documentoFile?: File;
  selfieFile?: File;
  documentoPreview?: string;
  selfiePreview?: string;

  readonly StatusVerificacao = StatusVerificacao;

  constructor(private verificacaoService: VerificacaoService) {}

  ngOnInit(): void {
    this.verificacaoService.consultarStatus().subscribe({
      next: v => { this.verificacao = v; this.loading = false; },
      error: err => {
        if (err.status === 204 || err.status === 404) {
          this.verificacao = null;
        }
        this.loading = false;
      }
    });
  }

  onDocumentoSelecionado(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.documentoFile = input.files[0];
      this.gerarPreview(this.documentoFile, url => this.documentoPreview = url);
    }
  }

  onSelfieSelecionada(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.selfieFile = input.files[0];
      this.gerarPreview(this.selfieFile, url => this.selfiePreview = url);
    }
  }

  private gerarPreview(file: File, cb: (url: string) => void): void {
    const reader = new FileReader();
    reader.onload = e => cb(e.target?.result as string);
    reader.readAsDataURL(file);
  }

  podeEnviar(): boolean {
    return !!(this.documentoFile && this.selfieFile) &&
           !this.enviando &&
           this.verificacao?.status !== StatusVerificacao.APROVADO;
  }

  enviar(): void {
    if (!this.documentoFile || !this.selfieFile) return;
    this.enviando = true;
    this.erro = '';
    this.sucesso = '';

    this.verificacaoService.enviarDocumentos(this.documentoFile, this.selfieFile).subscribe({
      next: v => {
        this.verificacao = v;
        this.sucesso = 'Documentos enviados com sucesso! Aguarde a revisão do admin.';
        this.documentoFile = undefined;
        this.selfieFile = undefined;
        this.documentoPreview = undefined;
        this.selfiePreview = undefined;
        this.enviando = false;
      },
      error: err => {
        this.erro = err.error?.message || 'Erro ao enviar documentos. Tente novamente.';
        this.enviando = false;
      }
    });
  }

  get badgeClasses(): string {
    const map: Record<string, string> = {
      PENDENTE: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      APROVADO: 'bg-green-100 text-green-800 border-green-200',
      RECUSADO: 'bg-red-100 text-red-800 border-red-200'
    };
    return map[this.verificacao?.status ?? ''] ?? 'bg-gray-100 text-gray-700';
  }

  get badgeLabel(): string {
    const map: Record<string, string> = {
      PENDENTE: '⏳ Aguardando revisão',
      APROVADO: '✅ Identidade verificada',
      RECUSADO: '❌ Recusado — reenvie os documentos'
    };
    return map[this.verificacao?.status ?? ''] ?? '';
  }
}
