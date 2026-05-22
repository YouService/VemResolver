import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CommonModule } from '@angular/common';
import { of, throwError } from 'rxjs';
import { VerificacaoDocumentoComponent } from './verificacao-documento.component';
import { VerificacaoService } from '../../services/verificacao.service';
import { StatusVerificacao, VerificacaoDocumentoDTO } from '../../models/models';

describe('VerificacaoDocumentoComponent', () => {
  let component: VerificacaoDocumentoComponent;
  let fixture: ComponentFixture<VerificacaoDocumentoComponent>;
  let verificacaoServiceSpy: jasmine.SpyObj<VerificacaoService>;

  beforeEach(async () => {
    verificacaoServiceSpy = jasmine.createSpyObj('VerificacaoService', ['consultarStatus', 'enviarDocumentos']);
    verificacaoServiceSpy.consultarStatus.and.returnValue(throwError(() => ({ status: 204 })));

    await TestBed.configureTestingModule({
      imports: [CommonModule, VerificacaoDocumentoComponent],
      providers: [{ provide: VerificacaoService, useValue: verificacaoServiceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(VerificacaoDocumentoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // ── VDC-01: criação ──────────────────────────────────────────────

  it('VDC-01: deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  // ── VDC-02: sem verificação → null ───────────────────────────────

  it('VDC-02: verificação null quando não há registro', () => {
    expect(component.verificacao).toBeNull();
    expect(component.loading).toBeFalse();
  });

  // ── VDC-03: exibe status PENDENTE ────────────────────────────────

  it('VDC-03: exibe badge PENDENTE quando status é PENDENTE', () => {
    const dto: VerificacaoDocumentoDTO = {
      id: 1, prestadorId: 10, prestadorNome: 'Maria',
      status: StatusVerificacao.PENDENTE, dataEnvio: '2026-05-22'
    };
    verificacaoServiceSpy.consultarStatus.and.returnValue(of(dto));
    component.ngOnInit();

    expect(component.verificacao?.status).toBe(StatusVerificacao.PENDENTE);
    expect(component.badgeLabel).toContain('Aguardando revisão');
  });

  // ── VDC-04: exibe status APROVADO ───────────────────────────────

  it('VDC-04: podeEnviar retorna false quando status é APROVADO', () => {
    component.verificacao = {
      id: 1, prestadorId: 10, prestadorNome: 'Maria',
      status: StatusVerificacao.APROVADO
    };
    component.documentoFile = new File(['x'], 'doc.jpg', { type: 'image/jpeg' });
    component.selfieFile = new File(['x'], 'selfie.jpg', { type: 'image/jpeg' });

    expect(component.podeEnviar()).toBeFalse();
  });

  // ── VDC-05: podeEnviar sem arquivos → false ──────────────────────

  it('VDC-05: podeEnviar retorna false sem arquivos selecionados', () => {
    component.verificacao = null;
    expect(component.podeEnviar()).toBeFalse();
  });

  // ── VDC-06: envio com sucesso ────────────────────────────────────

  it('VDC-06: após envio com sucesso, exibe mensagem de sucesso e zera arquivos', () => {
    const dto: VerificacaoDocumentoDTO = {
      id: 2, prestadorId: 10, prestadorNome: 'Maria',
      status: StatusVerificacao.PENDENTE
    };
    verificacaoServiceSpy.enviarDocumentos.and.returnValue(of(dto));
    component.documentoFile = new File(['x'], 'doc.jpg', { type: 'image/jpeg' });
    component.selfieFile = new File(['x'], 'selfie.jpg', { type: 'image/jpeg' });
    component.verificacao = null;

    component.enviar();

    expect(component.sucesso).toContain('sucesso');
    expect(component.documentoFile).toBeUndefined();
    expect(component.selfieFile).toBeUndefined();
  });

  // ── VDC-07: envio com erro ───────────────────────────────────────

  it('VDC-07: erro no envio exibe mensagem de erro', () => {
    verificacaoServiceSpy.enviarDocumentos.and.returnValue(
      throwError(() => ({ error: { message: 'Tipo não suportado' } }))
    );
    component.documentoFile = new File(['x'], 'doc.jpg', { type: 'image/jpeg' });
    component.selfieFile = new File(['x'], 'selfie.jpg', { type: 'image/jpeg' });
    component.verificacao = null;

    component.enviar();

    expect(component.erro).toContain('Tipo não suportado');
    expect(component.enviando).toBeFalse();
  });

  // ── VDC-08: badgeClasses por status ─────────────────────────────

  it('VDC-08: badgeClasses retorna classes corretas por status', () => {
    component.verificacao = { id: 1, prestadorId: 1, prestadorNome: 'x', status: StatusVerificacao.APROVADO };
    expect(component.badgeClasses).toContain('green');

    component.verificacao = { id: 1, prestadorId: 1, prestadorNome: 'x', status: StatusVerificacao.RECUSADO };
    expect(component.badgeClasses).toContain('red');
  });
});
