import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { PrestadorDashboardComponent } from './prestador-dashboard.component';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PrestadorService } from '../services/prestador.service';
import { PropostaService } from '../services/proposta.service';
import { AuthService } from '../services/auth.service';
import { Proposta, StatusProposta, StatusSolicitacao, Prestador, CompletudePrestador } from '../models/models';

const mockPrestador: Prestador = {
  id: 10, usuarioId: 1, categoriasIds: [1],
  cidade: 'São Paulo', estado: 'SP', avaliacaoMedia: 4.2, totalAvaliacoes: 5
};

const buildProposta = (
  status: StatusProposta,
  solicitacaoStatus: StatusSolicitacao,
  id = 1
): Proposta => ({
  id, solicitacaoId: 20, prestadorId: 10, prestadorNome: 'Maria',
  valor: 250, status, solicitacaoStatus, mensagem: 'ok'
});

describe('PrestadorDashboardComponent', () => {
  let component: PrestadorDashboardComponent;
  let fixture: ComponentFixture<PrestadorDashboardComponent>;
  let solicitacaoServiceSpy: jasmine.SpyObj<SolicitacaoService>;
  let prestadorServiceSpy: jasmine.SpyObj<PrestadorService>;
  let propostaServiceSpy: jasmine.SpyObj<PropostaService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    solicitacaoServiceSpy = jasmine.createSpyObj('SolicitacaoService', ['buscarAbertas']);
    prestadorServiceSpy = jasmine.createSpyObj('PrestadorService', ['buscarMeuPerfil', 'getCompletude']);
    propostaServiceSpy = jasmine.createSpyObj('PropostaService', ['listarMinhas']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUserName', 'getCurrentUser']);

    solicitacaoServiceSpy.buscarAbertas.and.returnValue(
      of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 })
    );
    prestadorServiceSpy.buscarMeuPerfil.and.returnValue(of(mockPrestador));
    prestadorServiceSpy.getCompletude.and.returnValue(of({ percentual: 0, itens: [] } as CompletudePrestador));
    propostaServiceSpy.listarMinhas.and.returnValue(
      of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 50 })
    );
    authServiceSpy.getCurrentUserName.and.returnValue('Maria');

    await TestBed.configureTestingModule({
      imports: [PrestadorDashboardComponent],
      providers: [
        provideRouter([]),
        { provide: SolicitacaoService, useValue: solicitacaoServiceSpy },
        { provide: PrestadorService, useValue: prestadorServiceSpy },
        { provide: PropostaService, useValue: propostaServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PrestadorDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve definir userName a partir do authService', () => {
    expect(component.userName).toBe('Maria');
  });

  it('deve carregar o perfil do prestador no ngOnInit', () => {
    expect(component.prestador).toEqual(mockPrestador);
  });

  // ── contratosAndamento ────────────────────────────────────────────────────────

  it('contratosAndamento deve retornar somente propostas ACEITA + EM_ANDAMENTO', () => {
    component.minhasPropostas = [
      buildProposta(StatusProposta.ACEITA, StatusSolicitacao.EM_ANDAMENTO, 1),
      buildProposta(StatusProposta.ACEITA, StatusSolicitacao.CONCLUIDA, 2),
      buildProposta(StatusProposta.ENVIADA, StatusSolicitacao.ABERTA, 3)
    ];
    expect(component.contratosAndamento.length).toBe(1);
    expect(component.contratosAndamento[0].id).toBe(1);
  });

  // ── propostasAguardando ───────────────────────────────────────────────────────

  it('propostasAguardando deve retornar somente propostas com status ENVIADA', () => {
    component.minhasPropostas = [
      buildProposta(StatusProposta.ENVIADA, StatusSolicitacao.ABERTA, 1),
      buildProposta(StatusProposta.ENVIADA, StatusSolicitacao.ABERTA, 2),
      buildProposta(StatusProposta.ACEITA, StatusSolicitacao.EM_ANDAMENTO, 3)
    ];
    expect(component.propostasAguardando.length).toBe(2);
  });

  // ── servicosConcluidos ────────────────────────────────────────────────────────

  it('servicosConcluidos deve retornar propostas cuja solicitação está CONCLUIDA', () => {
    component.minhasPropostas = [
      buildProposta(StatusProposta.ACEITA, StatusSolicitacao.CONCLUIDA, 1),
      buildProposta(StatusProposta.ACEITA, StatusSolicitacao.CONCLUIDA, 2),
      buildProposta(StatusProposta.ACEITA, StatusSolicitacao.EM_ANDAMENTO, 3)
    ];
    expect(component.servicosConcluidos.length).toBe(2);
  });

  // ── formatarData ──────────────────────────────────────────────────────────────

  it('formatarData deve retornar "Hoje" para a data atual', () => {
    const hoje = new Date();
    expect(component.formatarData(hoje)).toBe('Hoje');
  });

  it('formatarData deve retornar "Ontem" para a data de ontem', () => {
    const ontem = new Date();
    ontem.setDate(ontem.getDate() - 1);
    expect(component.formatarData(ontem)).toBe('Ontem');
  });

  it('formatarData deve retornar "N dias atrás" para datas dentro de 7 dias', () => {
    const tresAtras = new Date();
    tresAtras.setDate(tresAtras.getDate() - 3);
    expect(component.formatarData(tresAtras)).toBe('3 dias atrás');
  });

  it('formatarData deve retornar string vazia para data nula', () => {
    expect(component.formatarData(null)).toBe('');
  });

  // ── aplicarFiltro ──────────────────────────────────────────────────────────────

  it('aplicarFiltro sem termo retorna todas as solicitações', () => {
    component.solicitacoes = [
      { id: 1, categoriaId: 1, descricao: 'A', cidade: 'SP', estado: 'SP' },
      { id: 2, categoriaId: 1, descricao: 'B', cidade: 'RJ', estado: 'RJ' }
    ] as any[];
    component.filtroCidade = '';
    component.aplicarFiltro();
    expect(component.solicitacoesFiltradas.length).toBe(2);
  });

  it('aplicarFiltro com termo filtra por cidade', () => {
    component.solicitacoes = [
      { id: 1, categoriaId: 1, descricao: 'A', cidade: 'São Paulo', estado: 'SP' },
      { id: 2, categoriaId: 1, descricao: 'B', cidade: 'Rio de Janeiro', estado: 'RJ' }
    ] as any[];
    component.filtroCidade = 'paulo';
    component.aplicarFiltro();
    expect(component.solicitacoesFiltradas.length).toBe(1);
    expect(component.solicitacoesFiltradas[0].id).toBe(1);
  });
});
