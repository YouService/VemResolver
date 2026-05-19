import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PrestadorService } from '../../services/prestador.service';
import { AvaliacaoService } from '../../services/avaliacao.service';
import { FotoService } from '../../services/foto.service';
import { AuthService } from '../../services/auth.service';
import { AvaliacaoResumo, PerfilPublico } from '../../models/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-perfil-publico',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './perfil-publico.component.html'
})
export class PerfilPublicoComponent implements OnInit {
  perfil?: PerfilPublico;
  loading = true;
  naoEncontrado = false;
  linkCopiado = false;
  fotoAmpliada?: string;

  // Filtro por nota
  filtroNota: number | null = null;
  avaliacoesFiltradas?: AvaliacaoResumo[];
  carregandoFiltro = false;

  // Resposta a avaliações
  respostaAberta: Record<number, boolean> = {};
  textoResposta: Record<number, string> = {};
  enviandoResposta: Record<number, boolean> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private prestadorService: PrestadorService,
    private avaliacaoService: AvaliacaoService,
    private fotoService: FotoService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.naoEncontrado = true; this.loading = false; return; }
    this.prestadorService.getPerfilPublico(id).subscribe({
      next: (p) => { this.perfil = p; this.loading = false; },
      error: () => { this.naoEncontrado = true; this.loading = false; }
    });
  }

  get avaliacoesExibidas(): AvaliacaoResumo[] {
    return this.avaliacoesFiltradas ?? (this.perfil?.avaliacoes ?? []);
  }

  filtrarPorNota(nota: number | null): void {
    this.filtroNota = nota;
    if (nota === null) {
      this.avaliacoesFiltradas = undefined;
      return;
    }
    if (!this.perfil) return;
    this.carregandoFiltro = true;
    this.avaliacaoService.listarPorPrestador(this.perfil.id, 0, 20, nota).subscribe({
      next: (page) => {
        this.avaliacoesFiltradas = page.content.map(a => ({
          avaliacaoId: a.id,
          nomeAvaliador: a.nomeAvaliador ?? a.clienteNome ?? 'Anônimo',
          nota: a.nota,
          comentario: a.comentario,
          dataCriacao: a.dataCriacao,
          respostaPrestador: a.respostaPrestador,
          dataResposta: a.dataResposta
        }));
        this.carregandoFiltro = false;
      },
      error: () => { this.carregandoFiltro = false; }
    });
  }

  get isPrestadorDoPerfil(): boolean {
    if (!this.perfil || !this.authService.isAuthenticated() || !this.authService.isPrestador()) return false;
    return this.authService.getCurrentUser()?.userId === this.perfil.usuarioId;
  }

  abrirResposta(avaliacaoId: number): void {
    this.respostaAberta[avaliacaoId] = true;
    if (!this.textoResposta[avaliacaoId]) this.textoResposta[avaliacaoId] = '';
  }

  fecharResposta(avaliacaoId: number): void {
    this.respostaAberta[avaliacaoId] = false;
  }

  enviarResposta(avaliacao: AvaliacaoResumo): void {
    const id = avaliacao.avaliacaoId;
    if (!id) return;
    const texto = (this.textoResposta[id] ?? '').trim();
    if (!texto) return;
    this.enviandoResposta[id] = true;
    this.avaliacaoService.responder(id, texto).subscribe({
      next: (res) => {
        avaliacao.respostaPrestador = res.respostaPrestador;
        avaliacao.dataResposta = res.dataResposta;
        this.respostaAberta[id] = false;
        this.enviandoResposta[id] = false;
      },
      error: () => { this.enviandoResposta[id] = false; }
    });
  }

  get iniciaisNome(): string {
    return (this.perfil?.nome || '').split(' ').map(p => p[0]).slice(0, 2).join('').toUpperCase();
  }

  urlCompleta(url: string): string {
    return `${environment.baseUrl}${url}`;
  }

  solicitarServico(): void {
    if (this.authService.isAuthenticated() && this.authService.isCliente()) {
      this.router.navigate(['/cliente/nova-solicitacao']);
    } else {
      this.router.navigate(['/register'], { queryParams: { tipo: 'CLIENTE' } });
    }
  }

  copiarLink(): void {
    navigator.clipboard.writeText(window.location.href).then(() => {
      this.linkCopiado = true;
      setTimeout(() => { this.linkCopiado = false; }, 3000);
    });
  }

  ampliarFoto(url: string): void {
    this.fotoAmpliada = this.urlCompleta(url);
  }

  fecharFoto(): void {
    this.fotoAmpliada = undefined;
  }

  get botaoSolicitar(): string {
    if (!this.authService.isAuthenticated()) return 'Solicitar Serviço';
    if (this.authService.isCliente()) return 'Solicitar Serviço';
    if (this.authService.isPrestador()) return 'Compartilhar com Clientes';
    return 'Solicitar Serviço';
  }

  get mostrarBotaoSolicitar(): boolean {
    return !this.authService.isPrestador();
  }

  get temMediasDimensionais(): boolean {
    return !!(this.perfil?.mediaPontualidade || this.perfil?.mediaQualidade || this.perfil?.mediaComunicacao);
  }

  get dimensoesPerfil(): Array<{ label: string; valor: number }> {
    if (!this.perfil) return [];
    return [
      { label: 'Pontualidade', valor: this.perfil.mediaPontualidade! },
      { label: 'Qualidade', valor: this.perfil.mediaQualidade! },
      { label: 'Comunicação', valor: this.perfil.mediaComunicacao! }
    ].filter(d => d.valor != null);
  }
}
