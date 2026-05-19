import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PrestadorService } from '../../services/prestador.service';
import { FotoService } from '../../services/foto.service';
import { AuthService } from '../../services/auth.service';
import { PerfilPublico } from '../../models/models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-perfil-publico',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './perfil-publico.component.html'
})
export class PerfilPublicoComponent implements OnInit {
  perfil?: PerfilPublico;
  loading = true;
  naoEncontrado = false;
  linkCopiado = false;
  fotoAmpliada?: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private prestadorService: PrestadorService,
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
