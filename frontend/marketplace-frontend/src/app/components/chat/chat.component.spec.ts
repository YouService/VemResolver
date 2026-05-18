import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ChatComponent } from './chat.component';
import { ChatService } from '../../services/chat.service';
import { PropostaService } from '../../services/proposta.service';
import { AuthService } from '../../services/auth.service';
import { MensagemChat, StatusProposta, StatusSolicitacao, Proposta } from '../../models/models';

const mockProposta: Partial<Proposta> = {
  id: 20,
  status: StatusProposta.ENVIADA,
  solicitacaoStatus: StatusSolicitacao.ABERTA,
  solicitacaoDescricao: 'Troca de fiação',
  clienteNome: 'João',
  prestadorNome: 'Maria'
};

const mockMensagem = (id: number, remetenteId: number, texto: string): MensagemChat => ({
  id, propostaId: 20, remetenteId, remetenteNome: 'User' + remetenteId, texto,
  dataEnvio: new Date().toISOString(), lida: false
});

describe('ChatComponent', () => {
  let component: ChatComponent;
  let fixture: ComponentFixture<ChatComponent>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;
  let propostaServiceSpy: jasmine.SpyObj<PropostaService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    chatServiceSpy = jasmine.createSpyObj('ChatService', ['listar', 'enviar', 'marcarComoLidas']);
    propostaServiceSpy = jasmine.createSpyObj('PropostaService', ['buscarPorId']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    chatServiceSpy.listar.and.returnValue(of([]));
    chatServiceSpy.marcarComoLidas.and.returnValue(of(undefined as any));
    propostaServiceSpy.buscarPorId.and.returnValue(of(mockProposta as Proposta));
    authServiceSpy.getCurrentUser.and.returnValue({ userId: 1, nome: 'João', email: 'j@j.com', tipoUsuario: 'CLIENTE' } as any);

    await TestBed.configureTestingModule({
      imports: [ChatComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '20' } } } },
        { provide: Router, useValue: routerSpy },
        { provide: ChatService, useValue: chatServiceSpy },
        { provide: PropostaService, useValue: propostaServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    if (component['intervalo']) clearInterval(component['intervalo']);
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve inicializar propostaId e currentUserId corretamente', () => {
    expect(component.propostaId).toBe(20);
    expect(component.currentUserId).toBe(1);
  });

  it('deve carregar dados da proposta no ngOnInit', () => {
    expect(component.contratoDescricao).toBe('Troca de fiação');
    expect(component.clienteNome).toBe('João');
    expect(component.prestadorNome).toBe('Maria');
    expect(component.contratoEncerrado).toBeFalse();
  });

  it('deve setar contratoEncerrado=true quando proposta RECUSADA', () => {
    propostaServiceSpy.buscarPorId.and.returnValue(of({
      ...mockProposta, status: StatusProposta.RECUSADA
    } as Proposta));
    component.ngOnInit();
    expect(component.contratoEncerrado).toBeTrue();
  });

  it('deve setar contratoEncerrado=true quando solicitação CONCLUIDA', () => {
    propostaServiceSpy.buscarPorId.and.returnValue(of({
      ...mockProposta, solicitacaoStatus: StatusSolicitacao.CONCLUIDA
    } as Proposta));
    component.ngOnInit();
    expect(component.contratoEncerrado).toBeTrue();
  });

  // ── enviarMensagem ────────────────────────────────────────────────────────────

  it('enviarMensagem não envia quando texto está vazio', () => {
    component.textoMensagem = '   ';
    component.enviarMensagem();
    expect(chatServiceSpy.enviar).not.toHaveBeenCalled();
  });

  it('enviarMensagem não envia quando contratoEncerrado=true', () => {
    component.contratoEncerrado = true;
    component.textoMensagem = 'oi';
    component.enviarMensagem();
    expect(chatServiceSpy.enviar).not.toHaveBeenCalled();
  });

  it('enviarMensagem não envia quando texto excede 2000 caracteres', () => {
    component.textoMensagem = 'A'.repeat(2001);
    component.enviarMensagem();
    expect(chatServiceSpy.enviar).not.toHaveBeenCalled();
  });

  it('enviarMensagem adiciona mensagem otimista e chama chatService.enviar', () => {
    const msgReal = mockMensagem(99, 1, 'Oi');
    chatServiceSpy.enviar.and.returnValue(of(msgReal));
    chatServiceSpy.listar.and.returnValue(of([msgReal]));

    component.textoMensagem = 'Oi';
    component.enviarMensagem();

    expect(chatServiceSpy.enviar).toHaveBeenCalledWith(20, 'Oi');
  });

  it('enviarMensagem restaura texto em caso de erro', () => {
    chatServiceSpy.enviar.and.returnValue(throwError(() => new Error('Erro')));

    component.textoMensagem = 'Oi';
    component.enviarMensagem();

    expect(component.textoMensagem).toBe('Oi');
    expect(component.erro).toContain('Não foi possível enviar');
    expect(component.enviando).toBeFalse();
  });

  // ── caractereRestantes ────────────────────────────────────────────────────────

  it('caractereRestantes deve retornar 2000 com campo vazio', () => {
    component.textoMensagem = '';
    expect(component.caractereRestantes).toBe(2000);
  });

  it('caractereRestantes deve retornar 1995 com texto de 5 caracteres', () => {
    component.textoMensagem = 'Olá!!';
    expect(component.caractereRestantes).toBe(1995);
  });

  // ── isMinhaMsg ────────────────────────────────────────────────────────────────

  it('isMinhaMsg deve retornar true para mensagem do usuário atual', () => {
    const msg = mockMensagem(1, 1, 'minha');
    expect(component.isMinhaMsg(msg)).toBeTrue();
  });

  it('isMinhaMsg deve retornar false para mensagem de outro usuário', () => {
    const msg = mockMensagem(2, 2, 'dele');
    expect(component.isMinhaMsg(msg)).toBeFalse();
  });

  // ── formatarHora ──────────────────────────────────────────────────────────────

  it('formatarHora deve retornar formato HH:MM', () => {
    const dataFixa = new Date(2026, 4, 18, 14, 5).toISOString();
    const resultado = component.formatarHora(dataFixa);
    expect(resultado).toMatch(/^\d{2}:\d{2}$/);
  });

  // ── goBack ────────────────────────────────────────────────────────────────────

  it('goBack navega para /cliente/dashboard quando usuário é CLIENTE', () => {
    authServiceSpy.getCurrentUser.and.returnValue({ userId: 1, tipoUsuario: 'CLIENTE' } as any);
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/cliente/dashboard']);
  });

  it('goBack navega para /prestador/dashboard quando usuário é PRESTADOR', () => {
    authServiceSpy.getCurrentUser.and.returnValue({ userId: 2, tipoUsuario: 'PRESTADOR' } as any);
    component.goBack();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/prestador/dashboard']);
  });

  // ── ngOnDestroy ───────────────────────────────────────────────────────────────

  it('ngOnDestroy deve limpar o intervalo de polling', () => {
    const spy = spyOn(window, 'clearInterval');
    const intervalo = setInterval(() => {}, 10000);
    component['intervalo'] = intervalo;
    component.ngOnDestroy();
    expect(spy).toHaveBeenCalledWith(intervalo);
  });
});
