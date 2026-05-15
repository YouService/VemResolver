import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { AdminStats, UserResumoDTO } from '../../models/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  stats: AdminStats = { totalUsuarios: 0, totalPrestadores: 0, totalSolicitacoes: 0, totalPropostas: 0 };
  usuarios: UserResumoDTO[] = [];
  paginaAtual = 0;
  totalPaginas = 0;
  itensPorPagina = 10;
  loading = false;
  loadingStats = false;
  errorMessage = '';
  alterandoId: number | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.carregarStats();
    this.carregarUsuarios(0);
  }

  carregarStats(): void {
    this.loadingStats = true;
    this.adminService.buscarStats().subscribe({
      next: s => { this.stats = s; this.loadingStats = false; },
      error: () => { this.loadingStats = false; }
    });
  }

  carregarUsuarios(pagina: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.listarUsuarios(pagina, this.itensPorPagina).subscribe({
      next: res => {
        this.usuarios = res.content;
        this.totalPaginas = res.totalPages;
        this.paginaAtual = pagina;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar usuários. Tente novamente.';
        this.loading = false;
      }
    });
  }

  irParaPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas) {
      this.carregarUsuarios(pagina);
    }
  }

  alterarAtivacao(usuario: UserResumoDTO): void {
    this.alterandoId = usuario.id;
    this.adminService.alterarAtivacao(usuario.id, !usuario.ativo).subscribe({
      next: () => {
        usuario.ativo = !usuario.ativo;
        this.alterandoId = null;
      },
      error: () => {
        this.errorMessage = 'Erro ao alterar status do usuário.';
        this.alterandoId = null;
      }
    });
  }

  formatarData(data: string): string {
    if (!data) return '';
    return new Date(data).toLocaleDateString('pt-BR');
  }
}
