import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { NotificacaoService } from '../services/notificacao.service';
import { NotificacaoDTO } from '../models/models';
import { Router } from '@angular/router';
import { MENU_DATA } from '../models/menu-data';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {
  mobileMenuOpen = false;
  expandedCategory: string | null = null;
  menuData = MENU_DATA;
  notificacoes: NotificacaoDTO[] = [];
  mostrarPainel = false;

  constructor(
    public authService: AuthService,
    public notificacaoService: NotificacaoService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.notificacaoService.iniciarPolling();
  }

  logout(): void {
    this.notificacaoService.pararPolling();
    this.authService.logout();
    this.router.navigate(['/']);
    this.mobileMenuOpen = false;
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  toggleCategory(categoryId: string): void {
    if (this.expandedCategory === categoryId) {
      this.expandedCategory = null;
    } else {
      this.expandedCategory = categoryId;
    }
  }

  togglePainelNotificacoes(): void {
    this.mostrarPainel = !this.mostrarPainel;
    if (this.mostrarPainel) {
      this.notificacaoService.listarNaoLidas().subscribe({
        next: n => { this.notificacoes = n; },
        error: () => {}
      });
    }
  }

  abrirNotificacao(n: NotificacaoDTO): void {
    this.notificacaoService.marcarComoLida(n.id).subscribe();
    if (n.linkDestino) this.router.navigate([n.linkDestino]);
    this.mostrarPainel = false;
  }

  marcarTodasLidas(): void {
    this.notificacaoService.marcarTodasComoLidas().subscribe({
      next: () => { this.notificacoes = []; this.mostrarPainel = false; }
    });
  }

  formatarDataNotif(data: string): string {
    const d = new Date(data);
    const diff = Date.now() - d.getTime();
    const min = Math.floor(diff / 60000);
    if (min < 1) return 'agora mesmo';
    if (min < 60) return `${min} min atrás`;
    const h = Math.floor(min / 60);
    if (h < 24) return `${h}h atrás`;
    return d.toLocaleDateString('pt-BR');
  }

  fecharPainelSeForaClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.relative')) this.mostrarPainel = false;
  }
}
