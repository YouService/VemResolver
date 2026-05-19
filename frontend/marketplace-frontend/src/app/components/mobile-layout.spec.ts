import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { ClienteDashboardComponent } from './cliente-dashboard.component';
import { PrestadorDashboardComponent } from './prestador-dashboard.component';
import { By } from '@angular/platform-browser';

/**
 * P11.2 — Layout Otimizado para Mobile
 *
 * Verifica que os componentes principais têm as classes Tailwind
 * responsivas necessárias para uma boa experiência em telas pequenas.
 */

describe('P11.2 — ClienteDashboardComponent mobile layout', () => {
  let fixture: ComponentFixture<ClienteDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClienteDashboardComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        provideAnimationsAsync()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClienteDashboardComponent);
    fixture.detectChanges();
  });

  it('should have responsive header container (flex-col on mobile, flex-row on sm+)', () => {
    const compiled: HTMLElement = fixture.nativeElement;
    const header = compiled.querySelector('.flex-col.sm\\:flex-row');
    expect(header).withContext('Header deve ter flex-col sm:flex-row para empilhar em mobile').not.toBeNull();
  });

  it('should have responsive button group (flex-col on mobile)', () => {
    const compiled: HTMLElement = fixture.nativeElement;
    const buttonGroup = compiled.querySelector('.flex-col.sm\\:flex-row');
    expect(buttonGroup).withContext('Grupo de botões deve empilhar em mobile').not.toBeNull();
  });

  it('should use responsive title size (text-2xl on mobile, text-3xl on sm+)', () => {
    const compiled: HTMLElement = fixture.nativeElement;
    const title = compiled.querySelector('.text-2xl.sm\\:text-3xl');
    expect(title).withContext('Título deve ser menor em mobile').not.toBeNull();
  });
});

describe('P11.2 — PrestadorDashboardComponent mobile layout', () => {
  let fixture: ComponentFixture<PrestadorDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrestadorDashboardComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        provideAnimationsAsync()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PrestadorDashboardComponent);
    fixture.detectChanges();
  });

  it('should have responsive header container (flex-col on mobile)', () => {
    const compiled: HTMLElement = fixture.nativeElement;
    const header = compiled.querySelector('.flex-col.sm\\:flex-row');
    expect(header).withContext('Header deve ter flex-col sm:flex-row').not.toBeNull();
  });

  it('should have responsive title size', () => {
    const compiled: HTMLElement = fixture.nativeElement;
    const title = compiled.querySelector('.text-2xl.sm\\:text-3xl');
    expect(title).withContext('Título deve ser menor em mobile').not.toBeNull();
  });

  it('should have flex-wrap on the title/badge row to handle badge overflow on mobile', () => {
    const compiled: HTMLElement = fixture.nativeElement;
    const badgeRow = compiled.querySelector('.flex-wrap');
    expect(badgeRow).withContext('Linha do título/badge deve ter flex-wrap').not.toBeNull();
  });
});
