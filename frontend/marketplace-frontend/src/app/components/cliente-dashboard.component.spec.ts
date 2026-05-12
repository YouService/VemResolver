import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { ClienteDashboardComponent } from './cliente-dashboard.component';
import { SolicitacaoService } from '../services/solicitacao.service';
import { AuthService } from '../services/auth.service';
import { SolicitacaoServico, StatusSolicitacao } from '../models/models';

const mockSolicitacaoEmAndamento: SolicitacaoServico = {
  id: 10,
  clienteId: 1,
  clienteNome: 'João',
  categoriaId: 1,
  categoriaNome: 'Elétrica',
  descricao: 'Troca de fiação',
  cidade: 'São Paulo',
  estado: 'SP',
  status: StatusSolicitacao.EM_ANDAMENTO
};

describe('ClienteDashboardComponent', () => {
  let component: ClienteDashboardComponent;
  let fixture: ComponentFixture<ClienteDashboardComponent>;
  let solicitacaoServiceSpy: jasmine.SpyObj<SolicitacaoService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    solicitacaoServiceSpy = jasmine.createSpyObj('SolicitacaoService', [
      'listarMinhas', 'buscarPorId', 'deletar', 'concluir'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUserName']);

    solicitacaoServiceSpy.listarMinhas.and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 }));
    authServiceSpy.getCurrentUserName.and.returnValue('João');

    await TestBed.configureTestingModule({
      imports: [ClienteDashboardComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        { provide: SolicitacaoService, useValue: solicitacaoServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClienteDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('confirmarConclusao deve abrir modal e guardar solicitação', () => {
    component.confirmarConclusao(mockSolicitacaoEmAndamento);

    expect(component.mostrarModalConclusao).toBeTrue();
    expect(component.solicitacaoParaConcluir).toEqual(mockSolicitacaoEmAndamento);
  });

  it('fecharModalConclusao deve limpar estado do modal', () => {
    component.confirmarConclusao(mockSolicitacaoEmAndamento);
    component.fecharModalConclusao();

    expect(component.mostrarModalConclusao).toBeFalse();
    expect(component.solicitacaoParaConcluir).toBeNull();
  });

  it('executarConclusao deve chamar service.concluir e exibir mensagem de sucesso', fakeAsync(() => {
    solicitacaoServiceSpy.concluir.and.returnValue(of({ ...mockSolicitacaoEmAndamento, status: StatusSolicitacao.CONCLUIDA }));
    solicitacaoServiceSpy.listarMinhas.and.returnValue(of({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 10 }));

    component.confirmarConclusao(mockSolicitacaoEmAndamento);
    component.executarConclusao();

    expect(solicitacaoServiceSpy.concluir).toHaveBeenCalledWith(10);
    expect(component.mensagemSucesso).toContain('concluído');
    expect(component.mostrarModalConclusao).toBeFalse();

    tick(5000);
    expect(component.mensagemSucesso).toBe('');
  }));

  it('executarConclusao sem solicitacao selecionada nao chama o service', () => {
    component.solicitacaoParaConcluir = null;
    component.executarConclusao();

    expect(solicitacaoServiceSpy.concluir).not.toHaveBeenCalled();
  });

  it('executarConclusao com erro do service mantem modal fechado e nao exibe sucesso', () => {
    solicitacaoServiceSpy.concluir.and.returnValue(throwError(() => new Error('Erro')));

    component.confirmarConclusao(mockSolicitacaoEmAndamento);
    component.executarConclusao();

    expect(component.mensagemSucesso).toBe('');
    expect(component.concluindo).toBeFalse();
  });
});
