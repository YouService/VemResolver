import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminService } from '../../services/admin.service';
import { AdminStats, UserResumoDTO, SolicitacaoAdminDTO, PageResponse, ConfiguracaoEmailDTO } from '../../models/models';

const mockStats: AdminStats = {
  totalUsuarios: 25, totalPrestadores: 10, totalSolicitacoes: 43,
  totalPropostas: 38, usuariosOnline: 3
};

const mockUsuario: UserResumoDTO = {
  id: 1, nome: 'Joao', email: 'joao@test.com', tipo: 'CLIENTE',
  ativo: true, dataCadastro: '2026-01-01', avaliacaoMedia: 4.5,
  totalAvaliacoes: 2, online: false
};

const mockSol: SolicitacaoAdminDTO = {
  id: 10, clienteNome: 'Joao', clienteEmail: 'joao@test.com',
  categoriaNome: 'Eletricista', descricao: 'Troca de disjuntor',
  cidade: 'Sao Paulo', estado: 'SP', status: 'ABERTA',
  dataCriacao: '2026-05-01', totalPropostas: 2
};

const mockEmailConfig: ConfiguracaoEmailDTO = {
  brevoApiKey: 'xkey••••••••789',
  emailRemetente: 'noreply@vemresolver.com',
  nomeRemetente: 'VemResolver',
  ativo: true,
  configurado: true
};

const mockPage = <T>(items: T[]): PageResponse<T> => ({
  content: items, totalElements: items.length, totalPages: 1, number: 0, size: 15
});

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', [
      'buscarStats', 'listarUsuarios', 'listarOnline',
      'listarSolicitacoes', 'alterarAtivacao', 'alterarStatusSolicitacao',
      'getEmailConfig', 'salvarEmailConfig', 'testarEmail'
    ]);

    adminServiceSpy.buscarStats.and.returnValue(of(mockStats));
    adminServiceSpy.listarUsuarios.and.returnValue(of(mockPage([mockUsuario])));
    adminServiceSpy.listarOnline.and.returnValue(of([mockUsuario]));
    adminServiceSpy.listarSolicitacoes.and.returnValue(of(mockPage([mockSol])));
    adminServiceSpy.getEmailConfig.and.returnValue(of(mockEmailConfig));
    adminServiceSpy.salvarEmailConfig.and.returnValue(of(mockEmailConfig));
    adminServiceSpy.testarEmail.and.returnValue(of(undefined));

    await TestBed.configureTestingModule({
      imports: [AdminDashboardComponent],
      providers: [
        provideRouter([]),
        { provide: AdminService, useValue: adminServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve carregar stats no init', () => {
    expect(adminServiceSpy.buscarStats).toHaveBeenCalled();
    expect(component.stats.totalUsuarios).toBe(25);
    expect(component.stats.usuariosOnline).toBe(3);
  });

  it('deve carregar usuarios no init', () => {
    expect(adminServiceSpy.listarUsuarios).toHaveBeenCalled();
    expect(component.usuarios.length).toBe(1);
  });

  it('aba inicial deve ser usuarios', () => {
    expect(component.aba).toBe('usuarios');
  });

  // ── selecionarAba ─────────────────────────────────────────────────────────

  it('selecionarAba("online") deve carregar usuarios online', () => {
    component.selecionarAba('online');
    expect(adminServiceSpy.listarOnline).toHaveBeenCalled();
    expect(component.aba).toBe('online');
  });

  it('selecionarAba("solicitacoes") deve carregar solicitacoes', () => {
    component.selecionarAba('solicitacoes');
    expect(adminServiceSpy.listarSolicitacoes).toHaveBeenCalled();
    expect(component.solicitacoes.length).toBe(1);
  });

  it('selecionarAba("online") nao deve re-carregar se ja tem dados', () => {
    component.usuariosOnline = [mockUsuario];
    component.selecionarAba('online');
    expect(adminServiceSpy.listarOnline).not.toHaveBeenCalled();
  });

  it('selecionarAba("email") deve carregar config de email', () => {
    component.selecionarAba('email');
    expect(adminServiceSpy.getEmailConfig).toHaveBeenCalled();
    expect(component.aba).toBe('email');
    expect(component.emailConfig.configurado).toBeTrue();
  });

  // ── alterarAtivacao ───────────────────────────────────────────────────────

  it('alterarAtivacao deve inverter ativo do usuario', () => {
    adminServiceSpy.alterarAtivacao.and.returnValue(of(undefined));
    const u = { ...mockUsuario, ativo: true };
    component.alterarAtivacao(u);
    expect(u.ativo).toBeFalse();
  });

  it('alterarAtivacao deve exibir erro em caso de falha', () => {
    adminServiceSpy.alterarAtivacao.and.returnValue(throwError(() => new Error('fail')));
    component.alterarAtivacao({ ...mockUsuario });
    expect(component.errorMessage).toBeTruthy();
  });

  // ── alterarStatusSolicitacao ──────────────────────────────────────────────

  it('alterarStatusSolicitacao deve atualizar o status', () => {
    adminServiceSpy.alterarStatusSolicitacao.and.returnValue(of(undefined));
    const sol = { ...mockSol, status: 'ABERTA' };
    component.alterarStatusSolicitacao(sol, 'CONCLUIDA');
    expect(sol.status).toBe('CONCLUIDA');
  });

  it('alterarStatusSolicitacao nao deve chamar servico se status igual', () => {
    const sol = { ...mockSol, status: 'ABERTA' };
    component.alterarStatusSolicitacao(sol, 'ABERTA');
    expect(adminServiceSpy.alterarStatusSolicitacao).not.toHaveBeenCalled();
  });

  // ── E-mail config ─────────────────────────────────────────────────────────

  it('salvarEmailConfig deve chamar service e exibir sucesso', () => {
    component.emailForm = { emailRemetente: 'novo@test.com', nomeRemetente: 'Test', ativo: true };
    component.salvarEmailConfig();
    expect(adminServiceSpy.salvarEmailConfig).toHaveBeenCalled();
    expect(component.emailSucess).toBeTruthy();
  });

  it('salvarEmailConfig deve enviar apiKeyNova se preenchida', () => {
    component.emailForm = { emailRemetente: 'a@b.com', nomeRemetente: 'T', ativo: true, apiKeyNova: 'nova-key-abc' };
    component.salvarEmailConfig();
    const payload = adminServiceSpy.salvarEmailConfig.calls.mostRecent().args[0];
    expect(payload.brevoApiKey).toBe('nova-key-abc');
  });

  it('salvarEmailConfig deve exibir erro em falha', () => {
    adminServiceSpy.salvarEmailConfig.and.returnValue(throwError(() => new Error('erro')));
    component.emailForm = { ativo: false };
    component.salvarEmailConfig();
    expect(component.emailError).toBeTruthy();
  });

  it('enviarEmailTeste deve chamar service e exibir sucesso', () => {
    component.emailTesteDest = 'destino@test.com';
    component.enviarEmailTeste();
    expect(adminServiceSpy.testarEmail).toHaveBeenCalledWith('destino@test.com');
    expect(component.emailSucess).toContain('destino@test.com');
  });

  it('enviarEmailTeste nao deve chamar service se emailTesteDest vazio', () => {
    component.emailTesteDest = '';
    component.enviarEmailTeste();
    expect(adminServiceSpy.testarEmail).not.toHaveBeenCalled();
  });

  it('enviarEmailTeste deve exibir erro em falha', () => {
    adminServiceSpy.testarEmail.and.returnValue(throwError(() => new Error('erro')));
    component.emailTesteDest = 'fail@test.com';
    component.enviarEmailTeste();
    expect(component.emailError).toBeTruthy();
  });

  // ── badgeStatus / badgeTipo ───────────────────────────────────────────────

  it('badgeStatus deve retornar classe correta', () => {
    expect(component.badgeStatus('ABERTA')).toContain('blue');
    expect(component.badgeStatus('CONCLUIDA')).toContain('green');
    expect(component.badgeStatus('CANCELADA')).toContain('red');
  });

  // ── formatarData ──────────────────────────────────────────────────────────

  it('formatarData deve retornar string vazia para null', () => {
    expect(component.formatarData(null)).toBe('');
  });

  it('formatarDataHora deve retornar Nunca para null', () => {
    expect(component.formatarDataHora(null)).toBe('Nunca');
  });
});
