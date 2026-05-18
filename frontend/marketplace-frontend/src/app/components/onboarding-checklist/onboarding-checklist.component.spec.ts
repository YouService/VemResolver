import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

import { OnboardingChecklistComponent } from './onboarding-checklist.component';
import { PrestadorService } from '../../services/prestador.service';
import { CompletudePrestador, ItemChecklist } from '../../models/models';

const makeItem = (id: string, concluido: boolean, peso: number): ItemChecklist => ({
  id, descricao: `Item ${id}`, concluido, peso, linkDestino: '/prestador/perfil'
});

const makeCompletude = (percentual: number): CompletudePrestador => ({
  percentual,
  itens: [
    makeItem('foto', percentual >= 15, 15),
    makeItem('descricao', percentual >= 35, 20),
  ]
});

describe('OnboardingChecklistComponent', () => {
  let component: OnboardingChecklistComponent;
  let fixture: ComponentFixture<OnboardingChecklistComponent>;
  let prestadorServiceSpy: jasmine.SpyObj<PrestadorService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    prestadorServiceSpy = jasmine.createSpyObj('PrestadorService', ['getCompletude']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    prestadorServiceSpy.getCompletude.and.returnValue(of(makeCompletude(60)));

    await TestBed.configureTestingModule({
      imports: [OnboardingChecklistComponent],
      providers: [
        { provide: PrestadorService, useValue: prestadorServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OnboardingChecklistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('loading deve ser false após carregar', () => {
    expect(component.loading).toBeFalse();
    expect(component.completude).toBeDefined();
  });

  it('loading deve ser false mesmo em caso de erro da API', async () => {
    prestadorServiceSpy.getCompletude.and.returnValue(throwError(() => new Error('Erro')));
    component.carregar();
    expect(component.loading).toBeFalse();
  });

  // ── corBarra ────────────────────────────────────────────────────────────────

  it('corBarra deve ser bg-red-500 quando percentual < 50', () => {
    component.completude = makeCompletude(30);
    expect(component.corBarra).toBe('bg-red-500');
  });

  it('corBarra deve ser bg-yellow-500 quando percentual entre 50 e 79', () => {
    component.completude = makeCompletude(65);
    expect(component.corBarra).toBe('bg-yellow-500');
  });

  it('corBarra deve ser bg-green-500 quando percentual >= 80', () => {
    component.completude = makeCompletude(80);
    expect(component.corBarra).toBe('bg-green-500');
  });

  it('corBarra deve ser bg-gray-400 quando completude indefinida', () => {
    component.completude = undefined;
    expect(component.corBarra).toBe('bg-gray-400');
  });

  // ── completo ────────────────────────────────────────────────────────────────

  it('completo deve ser true quando percentual = 100', () => {
    component.completude = makeCompletude(100);
    expect(component.completo).toBeTrue();
  });

  it('completo deve ser false quando percentual < 100', () => {
    component.completude = makeCompletude(80);
    expect(component.completo).toBeFalse();
  });

  it('completo deve ser false quando completude for undefined', () => {
    component.completude = undefined;
    expect(component.completo).toBeFalse();
  });

  // ── auto-collapse a 100% ─────────────────────────────────────────────────────

  it('deve recolher automaticamente quando percentual = 100', () => {
    prestadorServiceSpy.getCompletude.and.returnValue(of(makeCompletude(100)));
    component.carregar();
    expect(component.recolhido).toBeTrue();
  });

  it('não deve recolher automaticamente quando percentual < 100', () => {
    prestadorServiceSpy.getCompletude.and.returnValue(of(makeCompletude(70)));
    component.recolhido = false;
    component.carregar();
    expect(component.recolhido).toBeFalse();
  });

  // ── toggleRecolhido ──────────────────────────────────────────────────────────

  it('toggleRecolhido deve alternar entre true e false', () => {
    component.recolhido = false;
    component.toggleRecolhido();
    expect(component.recolhido).toBeTrue();
    component.toggleRecolhido();
    expect(component.recolhido).toBeFalse();
  });

  // ── navegarItem ──────────────────────────────────────────────────────────────

  it('navegarItem deve navegar quando item não está concluído', () => {
    const item = makeItem('foto', false, 15);
    component.navegarItem(item);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/prestador/perfil']);
  });

  it('navegarItem não deve navegar quando item já está concluído', () => {
    const item = makeItem('foto', true, 15);
    component.navegarItem(item);
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});
