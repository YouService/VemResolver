import { Injectable } from '@angular/core';
import { SwPush } from '@angular/service-worker';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class PushService {
  private apiUrl = `${environment.apiUrl}/notificacoes/push`;

  constructor(
    private swPush: SwPush,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getPublicKey() {
    return this.http.get<{ publicKey: string }>(`${this.apiUrl}/public-key`);
  }

  async subscribeIfEnabled(): Promise<void> {
    if (!this.swPush.isEnabled || !this.authService.isAuthenticated()) return;
    try {
      const response = await this.getPublicKey().toPromise() as { publicKey: string } | undefined;
      const publicKey = response?.publicKey;
      if (!publicKey) return;
      const sub = await this.swPush.requestSubscription({ serverPublicKey: publicKey });
      const keys = (sub.toJSON() as any).keys;
      await this.http.post(`${this.apiUrl}/subscribe`, {
        endpoint: sub.endpoint,
        p256dh: keys.p256dh,
        auth: keys.auth
      }).toPromise();
    } catch (err) {
      // Push não disponível (sem HTTPS ou usuário negou permissão)
      console.debug('[Push] Não foi possível registrar push:', err);
    }
  }
}
