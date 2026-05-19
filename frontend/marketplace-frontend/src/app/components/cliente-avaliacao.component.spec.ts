import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ClienteAvaliacaoComponent } from './cliente-avaliacao.component';
import { AvaliacaoService } from '../services/avaliacao.service';
import { SolicitacaoService } from '../services/solicitacao.service';
import { PropostaService } from '../services/proposta.service';
import { StatusSolicitacao, StatusProposta } from '../models/models';

const mockSolicitacaoConcluida = {
  id: 5,
  clienteId: 1,
  clienteNome: 'João',
  categoriaId: 1,
  categoriaNome: 'Elétrica',
  descricao: 'Troca de tomadas',
  cidade: 'São Paulo',
  estado: 'SP',
  status: StatusSolicitacao.CONCLUIDA,
  foiAvaliadoPeloCliente: false
};

const mockPropostaAceita = {
  id: 10,
  prestadorId: 20,
  prestadorNome: 'Maria',
  status: StatusProposta.ACEITA
};

describe('ClienteAvaliacaoComponent', () => {
  let component: ClienteAvaliacaoComponent;
  let fixture: ComponentFixture<ClienteAvaliacaoComponent>;
  let avaliacaoServiceSpy: jasmine.SpyObj<AvaliacaoService>;
  let solicitacaoServiceSpy: jasmine.SpyObj<SolicitacaoService>;
  let propostaServiceSpy: jasmine.SpyObj<PropostaService>;

  beforeEach(async () => {
    avaliacaoServiceSpy = jasmine.createSpyObj('AvaliacaoService', ['criar']);
    solicitacaoServiceSpy = jasmine.createSpyObj('SolicitacaoService', ['buscarPorId']);
    propostaServiceSpy = jasmine.createSpyObj('PropostaService', ['listarPorSolicitacao']);

    solicitacaoServiceSpy.buscarPorId.and.returnValue(of(mockSolicitacaoConcluida as any));
    propostaServiceSpy.listarPorSolicitacao.and.returnValue(of({
      content: [mockPropostaAceita],
      totalElements: 1, totalPages: 1, number: 0, size: 20
    } as any));

    await TestBed.configureTestingModule({
      imports: [ClienteAvaliacaoComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: AvaliacaoService, useValue: avaliacaoServiceSpy },
        { provide: SolicitacaoService, useValue: solicitacaoServiceSpy },
        { provide: PropostaService, useValue: propostaServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => '5' } } }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClienteAvaliacaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── estrutura inicial ────────────────────────────────────────────────────

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar com 3 dimensões configuradas', () => {
    expect(component.dimensoes.length).toBe(3);
    expect(component.dimensoes[0].field).toBe('notaPontualidade');
    expect(component.dimensoes[1].field).toBe('notaQualidade');
    expect(component.dimensoes[2].field).toBe('notaComunicacao');
  });

  it('deve inicializar todas as notas dimensionais como 0', () => {
    component.dimensoes.forEach(dim => {
      expect(dim.nota).toBe(0);
      expect(dim.hover).toBe(0);
    });
  });

  // ── estrelaAtiva ─────────────────────────────────────────────────────────

  it('estrelaAtiva retorna true quando nota >= star', () => {
    const dim = component.dimensoes[0];
    dim.nota = 3;
    expect(component.estrelaAtiva(dim, 1)).toBeTrue();
    expect(component.estrelaAtiva(dim, 3)).toBeTrue();
    expect(component.estrelaAtiva(dim, 4)).toBeFalse();
  });

  it('estrelaAtiva usa hover quando hover > 0', () => {
    const dim = component.dimensoes[0];
    dim.nota = 2;
    dim.hover = 4;
    expect(component.estrelaAtiva(dim, 4)).toBeTrue();
    expect(component.estrelaAtiva(dim, 5)).toBeFalse();
  });

  // ── textoNota / classNota ────────────────────────────────────────────────

  it('textoNota retorna rótulo correto para cada nota', () => {
    expect(component.textoNota(1)).toBe('Péssimo');
    expect(component.textoNota(3)).toBe('Regular');
    expect(component.textoNota(5)).toBe('Excelente');
    expect(component.textoNota(0)).toBe('');
  });

  it('classNota retorna classe CSS correta', () => {
    expect(component.classNota(1)).toContain('red');
    expect(component.classNota(5)).toContain('green');
  });

  // ── enviar: sem nota geral ───────────────────────────────────────────────

  it('enviar não chama o serviço quando nota geral é 0', () => {
    component.nota = 0;
    component.prestadorId = 20;
    component.solicitacao = mockSolicitacaoConcluida as any;
    component.enviar();
    expect(avaliacaoServiceSpy.criar).not.toHaveBeenCalled();
    expect(component.tentouEnviar).toBeTrue();
  });

  // ── enviar: payload sem dimensões ────────────────────────────────────────

  it('enviar envia payload sem campos dimensionais quando nenhuma dimensão é selecionada', fakeAsync(() => {
    avaliacaoServiceSpy.criar.and.returnValue(of({} as any));
    component.nota = 4;
    component.prestadorId = 20;
    component.solicitacao = mockSolicitacaoConcluida as any;
    component.comentario = 'Ótimo serviço';

    component.enviar();
    tick();

    const args = avaliacaoServiceSpy.criar.calls.mostRecent().args[0] as any;
    expect(args.nota).toBe(4);
    expect(args.notaPontualidade).toBeUndefined();
    expect(args.notaQualidade).toBeUndefined();
    expect(args.notaComunicacao).toBeUndefined();
  }));

  // ── enviar: payload com todas as dimensões ───────────────────────────────

  it('enviar inclui dimensões no payload quando todas são selecionadas', fakeAsync(() => {
    avaliacaoServiceSpy.criar.and.returnValue(of({} as any));
    component.nota = 5;
    component.prestadorId = 20;
    component.solicitacao = mockSolicitacaoConcluida as any;
    component.dimensoes[0].nota = 5; // pontualidade
    component.dimensoes[1].nota = 4; // qualidade
    component.dimensoes[2].nota = 3; // comunicacao

    component.enviar();
    tick();

    const args = avaliacaoServiceSpy.criar.calls.mostRecent().args[0] as any;
    expect(args.notaPontualidade).toBe(5);
    expect(args.notaQualidade).toBe(4);
    expect(args.notaComunicacao).toBe(3);
  }));

  // ── enviar: payload com dimensões parciais ───────────────────────────────

  it('enviar inclui apenas dimensões com nota > 0 no payload', fakeAsync(() => {
    avaliacaoServiceSpy.criar.and.returnValue(of({} as any));
    component.nota = 4;
    component.prestadorId = 20;
    component.solicitacao = mockSolicitacaoConcluida as any;
    component.dimensoes[0].nota = 5; // pontualidade
    // qualidade e comunicacao ficam 0

    component.enviar();
    tick();

    const args = avaliacaoServiceSpy.criar.calls.mostRecent().args[0] as any;
    expect(args.notaPontualidade).toBe(5);
    expect(args.notaQualidade).toBeUndefined();
    expect(args.notaComunicacao).toBeUndefined();
  }));

  // ── enviar: sucesso ──────────────────────────────────────────────────────

  it('após envio com sucesso, enviado fica true e notaEnviada é preenchida', fakeAsync(() => {
    avaliacaoServiceSpy.criar.and.returnValue(of({} as any));
    component.nota = 5;
    component.prestadorId = 20;
    component.solicitacao = mockSolicitacaoConcluida as any;

    component.enviar();
    tick();

    expect(component.enviado).toBeTrue();
    expect(component.notaEnviada).toBe(5);
    expect(component.enviando).toBeFalse();
  }));

  // ── enviar: erro ─────────────────────────────────────────────────────────

  it('exibe mensagem de erro quando o backend retorna falha', fakeAsync(() => {
    avaliacaoServiceSpy.criar.and.returnValue(throwError(() => ({
      error: { message: 'já foi avaliada' }
    })));
    component.nota = 3;
    component.prestadorId = 20;
    component.solicitacao = mockSolicitacaoConcluida as any;

    component.enviar();
    tick();

    expect(component.enviando).toBeFalse();
    expect(component.erroEnvio).toContain('já foi avaliada');
  }));
});
