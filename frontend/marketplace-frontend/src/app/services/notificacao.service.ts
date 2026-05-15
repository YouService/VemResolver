import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NotificacaoDTO } from '../models/models';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificacaoService {
  private apiUrl = `${environment.apiUrl}/notificacoes`;
  private _count$ = new BehaviorSubject<number>(0);
  count$ = this._count$.asObservable();
  private intervalo?: ReturnType<typeof setInterval>;

  constructor(private http: HttpClient, private authService: AuthService) {}

  iniciarPolling(): void {
    this.atualizarContagem();
    this.intervalo = setInterval(() => this.atualizarContagem(), 30000);
  }

  pararPolling(): void {
    if (this.intervalo) clearInterval(this.intervalo);
  }

  atualizarContagem(): void {
    if (!this.authService.isAuthenticated()) return;
    this.http.get<{ count: number }>(`${this.apiUrl}/count`).subscribe({
      next: r => this._count$.next(r.count),
      error: () => {}
    });
  }

  listarNaoLidas(): Observable<NotificacaoDTO[]> {
    return this.http.get<NotificacaoDTO[]>(this.apiUrl);
  }

  marcarComoLida(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/lida`, null).pipe(
      tap(() => this.atualizarContagem())
    );
  }

  marcarTodasComoLidas(): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/ler-todas`, null).pipe(
      tap(() => this._count$.next(0))
    );
  }
}
