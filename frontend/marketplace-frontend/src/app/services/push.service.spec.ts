import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SwPush } from '@angular/service-worker';
import { PushService } from './push.service';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('PushService', () => {
  let service: PushService;
  let httpMock: HttpTestingController;
  let swPushSpy: jasmine.SpyObj<SwPush>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    swPushSpy = jasmine.createSpyObj('SwPush', ['requestSubscription'], { isEnabled: false });
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PushService,
        { provide: SwPush, useValue: swPushSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });

    service = TestBed.inject(PushService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('getPublicKey deve chamar o endpoint correto', () => {
    service.getPublicKey().subscribe(res => {
      expect(res.publicKey).toBe('test-key');
    });
    const req = httpMock.expectOne(`${environment.apiUrl}/notificacoes/push/public-key`);
    expect(req.request.method).toBe('GET');
    req.flush({ publicKey: 'test-key' });
  });

  it('subscribeIfEnabled não deve fazer nada quando SwPush não está habilitado', async () => {
    authServiceSpy.isAuthenticated.and.returnValue(true);
    // swPush.isEnabled = false (configurado no spy)
    await service.subscribeIfEnabled();
    expect(swPushSpy.requestSubscription).not.toHaveBeenCalled();
    httpMock.expectNone(`${environment.apiUrl}/notificacoes/push/public-key`);
  });

  it('subscribeIfEnabled não deve fazer nada quando usuário não está autenticado', async () => {
    Object.defineProperty(swPushSpy, 'isEnabled', { get: () => true });
    authServiceSpy.isAuthenticated.and.returnValue(false);
    await service.subscribeIfEnabled();
    expect(swPushSpy.requestSubscription).not.toHaveBeenCalled();
  });
});
