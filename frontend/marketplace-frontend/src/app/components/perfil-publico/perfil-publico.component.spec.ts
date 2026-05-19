import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { EMPTY, of, throwError } from 'rxjs';

import { PerfilPublicoComponent } from './perfil-publico.component';
import { PrestadorService } from '../../services/prestador.service';
import { FotoService } from '../../services/foto.service';
import { AuthService } from '../../services/auth.service';
import { PerfilPublico } from '../../models/models';

const mockPerfil: PerfilPublico = {
  id: 10,
  nome: 'Ana Paula Costa',
  fotoPerfilUrl: undefined,
  categorias: ['Eletricista', 'Encanador'],
  cidade: 'São Paulo',
  estado: 'SP',
  avaliacaoMedia: 4.5,
  totalAvaliacoes: 12,
  verificado: true,
  portafolioUrls: ['/uploads/f1.jpg', '/uploads/f2.jpg'],
  avaliacoes: [{ nomeAvaliador: 'João', nota: 5, comentario: 'Ótimo!', dataCriacao: '2026-05-01' }]
};

describe('PerfilPublicoComponent', () => {
  let component: PerfilPublicoComponent;
  let fixture: ComponentFixture<PerfilPublicoComponent>;
  let prestadorServiceSpy: jasmine.SpyObj<PrestadorService>;
  let fotoServiceSpy: jasmine.SpyObj<FotoService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  function createComponent(paramId = '10', isAutenticado = false, isCliente = false, isPrestador = false) {
    prestadorServiceSpy = jasmine.createSpyObj('PrestadorService', ['getPerfilPublico']);
    fotoServiceSpy = jasmine.createSpyObj('FotoService', ['urlCompleta']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated', 'isCliente', 'isPrestador']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'createUrlTree', 'serializeUrl'], { events: EMPTY, url: '/' });
    routerSpy.createUrlTree.and.returnValue(null as any);
    routerSpy.serializeUrl.and.returnValue('/');

    authServiceSpy.isAuthenticated.and.returnValue(isAutenticado);
    authServiceSpy.isCliente.and.returnValue(isCliente);
    authServiceSpy.isPrestador.and.returnValue(isPrestador);
    prestadorServiceSpy.getPerfilPublico.and.returnValue(of(mockPerfil));

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [PerfilPublicoComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => paramId } } }
        },
        { provide: Router, useValue: routerSpy },
        { provide: PrestadorService, useValue: prestadorServiceSpy },
        { provide: FotoService, useValue: fotoServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilPublicoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => createComponent());

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve carregar o perfil e definir loading=false', () => {
    expect(component.loading).toBeFalse();
    expect(component.perfil).toEqual(mockPerfil);
    expect(component.naoEncontrado).toBeFalse();
  });

  it('deve definir naoEncontrado=true quando a API retorna erro', () => {
    createComponent('10');
    prestadorServiceSpy.getPerfilPublico.and.returnValue(throwError(() => new Error('404')));
    component.naoEncontrado = false;
    component.ngOnInit();
    expect(component.naoEncontrado).toBeTrue();
    expect(component.loading).toBeFalse();
  });

  it('deve definir naoEncontrado=true quando id = 0 (param inválido)', () => {
    createComponent('0');
    expect(component.naoEncontrado).toBeTrue();
    expect(component.loading).toBeFalse();
  });

  // ── iniciaisNome ─────────────────────────────────────────────────────────────

  it('iniciaisNome deve retornar as duas primeiras iniciais do nome', () => {
    expect(component.iniciaisNome).toBe('AP');
  });

  it('iniciaisNome deve retornar string vazia quando perfil não carregado', () => {
    component.perfil = undefined;
    expect(component.iniciaisNome).toBe('');
  });

  // ── mostrarBotaoSolicitar ────────────────────────────────────────────────────

  it('mostrarBotaoSolicitar deve ser true quando visitante', () => {
    createComponent('10', false, false, false);
    expect(component.mostrarBotaoSolicitar).toBeTrue();
  });

  it('mostrarBotaoSolicitar deve ser true quando cliente logado', () => {
    createComponent('10', true, true, false);
    expect(component.mostrarBotaoSolicitar).toBeTrue();
  });

  it('mostrarBotaoSolicitar deve ser false quando prestador logado', () => {
    createComponent('10', true, false, true);
    expect(component.mostrarBotaoSolicitar).toBeFalse();
  });

  // ── botaoSolicitar (texto) ───────────────────────────────────────────────────

  it('botaoSolicitar deve ser "Solicitar Serviço" para visitante', () => {
    createComponent('10', false, false, false);
    expect(component.botaoSolicitar).toBe('Solicitar Serviço');
  });

  it('botaoSolicitar deve ser "Solicitar Serviço" para cliente logado', () => {
    createComponent('10', true, true, false);
    expect(component.botaoSolicitar).toBe('Solicitar Serviço');
  });

  it('botaoSolicitar deve ser "Compartilhar com Clientes" para prestador logado', () => {
    createComponent('10', true, false, true);
    expect(component.botaoSolicitar).toBe('Compartilhar com Clientes');
  });

  // ── solicitarServico ─────────────────────────────────────────────────────────

  it('solicitarServico navega para /register quando visitante', () => {
    createComponent('10', false, false, false);
    component.solicitarServico();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/register'], { queryParams: { tipo: 'CLIENTE' } });
  });

  it('solicitarServico navega para /cliente/nova-solicitacao quando cliente logado', () => {
    createComponent('10', true, true, false);
    component.solicitarServico();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/cliente/nova-solicitacao']);
  });

  // ── ampliarFoto / fecharFoto ─────────────────────────────────────────────────

  it('ampliarFoto define fotoAmpliada com URL completa', () => {
    component.ampliarFoto('/uploads/foto.jpg');
    expect(component.fotoAmpliada).toContain('/uploads/foto.jpg');
  });

  it('fecharFoto limpa fotoAmpliada', () => {
    component.fotoAmpliada = 'alguma-url';
    component.fecharFoto();
    expect(component.fotoAmpliada).toBeUndefined();
  });

  // ── temMediasDimensionais ────────────────────────────────────────────────────

  it('temMediasDimensionais deve ser false quando nenhuma média dimensional é definida', () => {
    component.perfil = { ...mockPerfil, mediaPontualidade: undefined, mediaQualidade: undefined, mediaComunicacao: undefined };
    expect(component.temMediasDimensionais).toBeFalse();
  });

  it('temMediasDimensionais deve ser false quando perfil não está carregado', () => {
    component.perfil = undefined;
    expect(component.temMediasDimensionais).toBeFalse();
  });

  it('temMediasDimensionais deve ser true quando apenas mediaPontualidade está definida', () => {
    component.perfil = { ...mockPerfil, mediaPontualidade: 4.5, mediaQualidade: undefined, mediaComunicacao: undefined };
    expect(component.temMediasDimensionais).toBeTrue();
  });

  it('temMediasDimensionais deve ser true quando todas as médias dimensionais estão definidas', () => {
    component.perfil = { ...mockPerfil, mediaPontualidade: 4.8, mediaQualidade: 4.5, mediaComunicacao: 4.2 };
    expect(component.temMediasDimensionais).toBeTrue();
  });

  // ── dimensoesPerfil ──────────────────────────────────────────────────────────

  it('dimensoesPerfil deve retornar array vazio quando perfil não está carregado', () => {
    component.perfil = undefined;
    expect(component.dimensoesPerfil).toEqual([]);
  });

  it('dimensoesPerfil deve retornar array vazio quando nenhuma média é definida', () => {
    component.perfil = { ...mockPerfil, mediaPontualidade: undefined, mediaQualidade: undefined, mediaComunicacao: undefined };
    expect(component.dimensoesPerfil.length).toBe(0);
  });

  it('dimensoesPerfil deve retornar apenas as dimensões com valor definido', () => {
    component.perfil = { ...mockPerfil, mediaPontualidade: 4.8, mediaQualidade: undefined, mediaComunicacao: 4.0 };
    const dims = component.dimensoesPerfil;
    expect(dims.length).toBe(2);
    expect(dims.map(d => d.label)).toEqual(['Pontualidade', 'Comunicação']);
  });

  it('dimensoesPerfil deve retornar todas as 3 dimensões quando todas estão definidas', () => {
    component.perfil = { ...mockPerfil, mediaPontualidade: 4.8, mediaQualidade: 4.5, mediaComunicacao: 4.2 };
    const dims = component.dimensoesPerfil;
    expect(dims.length).toBe(3);
    expect(dims[0]).toEqual({ label: 'Pontualidade', valor: 4.8 });
    expect(dims[1]).toEqual({ label: 'Qualidade', valor: 4.5 });
    expect(dims[2]).toEqual({ label: 'Comunicação', valor: 4.2 });
  });
});
