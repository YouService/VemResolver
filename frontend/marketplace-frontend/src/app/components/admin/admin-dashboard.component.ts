import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { VerificacaoService } from '../../services/verificacao.service';
import { DenunciaService } from '../../services/denuncia.service';
import { AdminStats, UserResumoDTO, SolicitacaoAdminDTO, ConfiguracaoEmailDTO, StatusVerificacao, VerificacaoDocumentoDTO, DenunciaDTO, StatusDenuncia } from '../../models/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  stats: AdminStats = { totalUsuarios: 0, totalPrestadores: 0, totalSolicitacoes: 0, totalPropostas: 0, usuariosOnline: 0, verificacoesPendentes: 0, denunciasPendentes: 0 };
  usuarios: UserResumoDTO[] = [];
  usuariosOnline: UserResumoDTO[] = [];
  solicitacoes: SolicitacaoAdminDTO[] = [];

  aba: 'usuarios' | 'online' | 'solicitacoes' | 'email' | 'verificacoes' | 'denuncias' = 'usuarios';

  paginaAtual = 0;
  totalPaginas = 0;
  itensPorPagina = 10;

  paginaSol = 0;
  totalPaginasSol = 0;
  itensPorPaginaSol = 15;
  filtroStatus = '';

  loading = false;
  loadingStats = false;
  loadingOnline = false;
  loadingSol = false;
  errorMessage = '';
  alterandoId: number | null = null;
  alterandoSolId: number | null = null;

  readonly statusOpcoes = ['', 'ABERTA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'];

  // Email config
  emailConfig: ConfiguracaoEmailDTO = { ativo: false };
  emailForm: ConfiguracaoEmailDTO & { apiKeyNova?: string } = { ativo: false };
  loadingEmail = false;
  savingEmail = false;
  testingEmail = false;
  emailTesteDest = '';
  emailSucess = '';
  emailError = '';
  mostrarApiKey = false;

  // Verificações
  verificacoes: VerificacaoDocumentoDTO[] = [];
  loadingVerif = false;
  paginaVerif = 0;
  totalPaginasVerif = 0;
  itensPorPaginaVerif = 15;
  filtroVerifStatus: StatusVerificacao | '' = StatusVerificacao.PENDENTE;
  observacaoVerif: Record<number, string> = {};
  processandoVerifId: number | null = null;
  verificSucesso = '';
  verificErro = '';

  readonly StatusVerificacao = StatusVerificacao;
  readonly statusVerifOpcoes = [
    { valor: '', label: 'Todas' },
    { valor: StatusVerificacao.PENDENTE, label: 'Pendentes' },
    { valor: StatusVerificacao.APROVADO, label: 'Aprovadas' },
    { valor: StatusVerificacao.RECUSADO, label: 'Recusadas' }
  ];

  // Denúncias
  denuncias: DenunciaDTO[] = [];
  loadingDenuncias = false;
  paginaDenuncias = 0;
  totalPaginasDenuncias = 0;
  itensPorPaginaDenuncias = 15;
  filtroDenunciaStatus: StatusDenuncia | '' = StatusDenuncia.PENDENTE;
  processandoDenunciaId: number | null = null;
  observacaoDenuncia: Record<number, string> = {};
  denunciaSucesso = '';
  denunciaErro = '';

  readonly StatusDenuncia = StatusDenuncia;
  readonly statusDenunciaOpcoes = [
    { valor: '', label: 'Todas' },
    { valor: StatusDenuncia.PENDENTE, label: 'Pendentes' },
    { valor: StatusDenuncia.APROVADA, label: 'Aprovadas' },
    { valor: StatusDenuncia.REJEITADA, label: 'Rejeitadas' }
  ];

  constructor(
    private adminService: AdminService,
    private verificacaoService: VerificacaoService,
    private denunciaService: DenunciaService
  ) {}

  ngOnInit(): void {
    this.carregarStats();
    this.carregarUsuarios(0);
  }

  selecionarAba(aba: 'usuarios' | 'online' | 'solicitacoes' | 'email' | 'verificacoes' | 'denuncias'): void {
    this.aba = aba;
    this.errorMessage = '';
    this.emailSucess = '';
    this.emailError = '';
    if (aba === 'online' && this.usuariosOnline.length === 0) this.carregarOnline();
    if (aba === 'solicitacoes' && this.solicitacoes.length === 0) this.carregarSolicitacoes(0);
    if (aba === 'email') this.carregarEmailConfig();
    if (aba === 'verificacoes') this.carregarVerificacoes(0);
    if (aba === 'denuncias') this.carregarDenuncias(0);
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
        this.errorMessage = 'Erro ao carregar usuários.';
        this.loading = false;
      }
    });
  }

  carregarOnline(): void {
    this.loadingOnline = true;
    this.errorMessage = '';
    this.adminService.listarOnline().subscribe({
      next: lista => { this.usuariosOnline = lista; this.loadingOnline = false; },
      error: () => { this.errorMessage = 'Erro ao carregar usuários online.'; this.loadingOnline = false; }
    });
  }

  carregarSolicitacoes(pagina: number): void {
    this.loadingSol = true;
    this.errorMessage = '';
    const status = this.filtroStatus || undefined;
    this.adminService.listarSolicitacoes(pagina, this.itensPorPaginaSol, status).subscribe({
      next: res => {
        this.solicitacoes = res.content;
        this.totalPaginasSol = res.totalPages;
        this.paginaSol = pagina;
        this.loadingSol = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar solicitações.';
        this.loadingSol = false;
      }
    });
  }

  aplicarFiltroStatus(): void {
    this.carregarSolicitacoes(0);
  }

  irParaPagina(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginas) this.carregarUsuarios(pagina);
  }

  irParaPaginaSol(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginasSol) this.carregarSolicitacoes(pagina);
  }

  alterarAtivacao(usuario: UserResumoDTO): void {
    this.alterandoId = usuario.id;
    this.adminService.alterarAtivacao(usuario.id, !usuario.ativo).subscribe({
      next: () => { usuario.ativo = !usuario.ativo; this.alterandoId = null; },
      error: () => { this.errorMessage = 'Erro ao alterar status do usuário.'; this.alterandoId = null; }
    });
  }

  alterarStatusSolicitacao(sol: SolicitacaoAdminDTO, novoStatus: string): void {
    if (!novoStatus || novoStatus === sol.status) return;
    this.alterandoSolId = sol.id;
    this.adminService.alterarStatusSolicitacao(sol.id, novoStatus).subscribe({
      next: () => { sol.status = novoStatus; this.alterandoSolId = null; },
      error: () => { this.errorMessage = 'Erro ao alterar status da solicitação.'; this.alterandoSolId = null; }
    });
  }

  // ── Email config ────────────────────────────────────────────────

  carregarEmailConfig(): void {
    this.loadingEmail = true;
    this.adminService.getEmailConfig().subscribe({
      next: cfg => {
        this.emailConfig = cfg;
        this.emailForm = {
          emailRemetente: cfg.emailRemetente ?? '',
          nomeRemetente: cfg.nomeRemetente ?? 'VemResolver',
          ativo: cfg.ativo ?? false,
          apiKeyNova: ''
        };
        this.loadingEmail = false;
      },
      error: () => { this.emailError = 'Erro ao carregar configuração de e-mail.'; this.loadingEmail = false; }
    });
  }

  salvarEmailConfig(): void {
    this.savingEmail = true;
    this.emailSucess = '';
    this.emailError = '';
    const payload: ConfiguracaoEmailDTO = {
      emailRemetente: this.emailForm.emailRemetente,
      nomeRemetente:  this.emailForm.nomeRemetente,
      ativo:          this.emailForm.ativo
    };
    if (this.emailForm.apiKeyNova && this.emailForm.apiKeyNova.trim()) {
      payload.brevoApiKey = this.emailForm.apiKeyNova.trim();
    }
    this.adminService.salvarEmailConfig(payload).subscribe({
      next: cfg => {
        this.emailConfig = cfg;
        this.emailForm.apiKeyNova = '';
        this.emailSucess = 'Configuração salva com sucesso!';
        this.savingEmail = false;
      },
      error: () => { this.emailError = 'Erro ao salvar configuração.'; this.savingEmail = false; }
    });
  }

  enviarEmailTeste(): void {
    if (!this.emailTesteDest) return;
    this.testingEmail = true;
    this.emailSucess = '';
    this.emailError = '';
    this.adminService.testarEmail(this.emailTesteDest).subscribe({
      next: () => { this.emailSucess = 'E-mail de teste enviado para ' + this.emailTesteDest; this.testingEmail = false; },
      error: () => { this.emailError = 'Erro ao enviar e-mail de teste. Verifique a configuração.'; this.testingEmail = false; }
    });
  }

  // ── Verificações ─────────────────────────────────────────────────

  carregarVerificacoes(pagina: number): void {
    this.loadingVerif = true;
    this.verificSucesso = '';
    this.verificErro = '';
    const s = this.filtroVerifStatus || undefined;
    this.verificacaoService.listar(s as StatusVerificacao | undefined, pagina, this.itensPorPaginaVerif).subscribe({
      next: res => {
        this.verificacoes = res.content;
        this.totalPaginasVerif = res.totalPages;
        this.paginaVerif = pagina;
        this.loadingVerif = false;
      },
      error: () => { this.verificErro = 'Erro ao carregar verificações.'; this.loadingVerif = false; }
    });
  }

  aplicarFiltroVerif(): void {
    this.carregarVerificacoes(0);
  }

  irParaPaginaVerif(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginasVerif) this.carregarVerificacoes(pagina);
  }

  aprovarVerificacao(v: VerificacaoDocumentoDTO): void {
    this.processandoVerifId = v.id;
    this.verificacaoService.aprovar(v.id, this.observacaoVerif[v.id]).subscribe({
      next: atualizado => {
        const idx = this.verificacoes.findIndex(x => x.id === v.id);
        if (idx >= 0) this.verificacoes[idx] = atualizado;
        this.verificSucesso = `Verificação de ${atualizado.prestadorNome} aprovada.`;
        this.processandoVerifId = null;
        this.carregarStats();
      },
      error: () => { this.verificErro = 'Erro ao aprovar verificação.'; this.processandoVerifId = null; }
    });
  }

  recusarVerificacao(v: VerificacaoDocumentoDTO): void {
    this.processandoVerifId = v.id;
    this.verificacaoService.recusar(v.id, this.observacaoVerif[v.id]).subscribe({
      next: atualizado => {
        const idx = this.verificacoes.findIndex(x => x.id === v.id);
        if (idx >= 0) this.verificacoes[idx] = atualizado;
        this.verificSucesso = `Verificação de ${atualizado.prestadorNome} recusada.`;
        this.processandoVerifId = null;
      },
      error: () => { this.verificErro = 'Erro ao recusar verificação.'; this.processandoVerifId = null; }
    });
  }

  urlDocumento(id: number): string { return this.verificacaoService.urlDocumento(id); }
  urlSelfie(id: number): string { return this.verificacaoService.urlSelfie(id); }

  // ── Denúncias ────────────────────────────────────────────────────

  carregarDenuncias(pagina: number): void {
    this.loadingDenuncias = true;
    this.denunciaSucesso = '';
    this.denunciaErro = '';
    const s = this.filtroDenunciaStatus || undefined;
    this.denunciaService.listar(s as StatusDenuncia | undefined, pagina, this.itensPorPaginaDenuncias).subscribe({
      next: res => {
        this.denuncias = res.content;
        this.totalPaginasDenuncias = res.totalPages;
        this.paginaDenuncias = pagina;
        this.loadingDenuncias = false;
      },
      error: () => { this.denunciaErro = 'Erro ao carregar denúncias.'; this.loadingDenuncias = false; }
    });
  }

  aplicarFiltroDenuncia(): void {
    this.carregarDenuncias(0);
  }

  irParaPaginaDenuncia(pagina: number): void {
    if (pagina >= 0 && pagina < this.totalPaginasDenuncias) this.carregarDenuncias(pagina);
  }

  aprovarDenuncia(d: DenunciaDTO): void {
    this.processandoDenunciaId = d.id;
    this.denunciaService.atualizarStatus(d.id, StatusDenuncia.APROVADA, this.observacaoDenuncia[d.id]).subscribe({
      next: atualizado => {
        const idx = this.denuncias.findIndex(x => x.id === d.id);
        if (idx >= 0) this.denuncias[idx] = atualizado;
        this.denunciaSucesso = `Denúncia contra ${atualizado.denunciadoNome} aprovada.`;
        this.processandoDenunciaId = null;
        this.carregarStats();
      },
      error: (err) => {
        this.denunciaErro = err?.error?.message || 'Erro ao aprovar denúncia.';
        this.processandoDenunciaId = null;
      }
    });
  }

  rejeitarDenuncia(d: DenunciaDTO): void {
    this.processandoDenunciaId = d.id;
    this.denunciaService.atualizarStatus(d.id, StatusDenuncia.REJEITADA, this.observacaoDenuncia[d.id]).subscribe({
      next: atualizado => {
        const idx = this.denuncias.findIndex(x => x.id === d.id);
        if (idx >= 0) this.denuncias[idx] = atualizado;
        this.denunciaSucesso = `Denúncia rejeitada.`;
        this.processandoDenunciaId = null;
        this.carregarStats();
      },
      error: (err) => {
        this.denunciaErro = err?.error?.message || 'Erro ao rejeitar denúncia.';
        this.processandoDenunciaId = null;
      }
    });
  }

  badgeDenuncia(status: string): string {
    const map: Record<string, string> = {
      PENDENTE: 'bg-yellow-100 text-yellow-800',
      APROVADA: 'bg-red-100 text-red-800',
      REJEITADA: 'bg-gray-100 text-gray-700'
    };
    return map[status] ?? 'bg-gray-100 text-gray-700';
  }

  badgeVerif(status: string): string {
    const map: Record<string, string> = {
      PENDENTE: 'bg-yellow-100 text-yellow-800',
      APROVADO: 'bg-green-100 text-green-800',
      RECUSADO: 'bg-red-100 text-red-800'
    };
    return map[status] ?? 'bg-gray-100 text-gray-700';
  }

  // ── Helpers ─────────────────────────────────────────────────────

  badgeStatus(status: string): string {
    const map: Record<string, string> = {
      ABERTA: 'bg-blue-100 text-blue-800',
      EM_ANDAMENTO: 'bg-yellow-100 text-yellow-800',
      CONCLUIDA: 'bg-green-100 text-green-800',
      CANCELADA: 'bg-red-100 text-red-800'
    };
    return map[status] ?? 'bg-gray-100 text-gray-700';
  }

  badgeTipo(tipo: string): string {
    const map: Record<string, string> = {
      PRESTADOR: 'bg-blue-100 text-blue-800',
      ADMIN: 'bg-purple-100 text-purple-800',
      CLIENTE: 'bg-gray-100 text-gray-700'
    };
    return map[tipo] ?? 'bg-gray-100 text-gray-700';
  }

  formatarData(data: string | null | undefined): string {
    if (!data) return '';
    return new Date(data).toLocaleDateString('pt-BR');
  }

  formatarDataHora(data: string | null | undefined): string {
    if (!data) return 'Nunca';
    return new Date(data).toLocaleString('pt-BR');
  }
}
