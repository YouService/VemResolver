import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, throwError } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, TipoUsuario, User, UpdatePerfilRequest, AlterarSenhaRequest } from '../models/models';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  private getUserFromStorage(): AuthResponse | null {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, data).pipe(
      tap(response => {
        localStorage.setItem('currentUser', JSON.stringify(response));
        localStorage.setItem('token', response.token);
        this.currentUserSubject.next(response);
      })
    );
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, data).pipe(
      tap(response => {
        localStorage.setItem('currentUser', JSON.stringify(response));
        localStorage.setItem('token', response.token);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        this.currentUserSubject.next(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  getCurrentUserId(): number | null {
    return this.currentUserSubject.value?.userId || null;
  }

  getCurrentUserName(): string | null {
    return this.currentUserSubject.value?.nome || null;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    const refreshToken = localStorage.getItem('refreshToken');
    if (!token && !refreshToken) return false;
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.exp * 1000 > Date.now()) return true;
      } catch {
        // token malformado
      }
    }
    // token expirado mas há refresh token — o interceptor renova na próxima chamada
    return !!refreshToken;
  }

  refresh(refreshToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(response => {
        localStorage.setItem('currentUser', JSON.stringify(response));
        localStorage.setItem('token', response.token);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        this.currentUserSubject.next(response);
      })
    );
  }

  isCliente(): boolean {
    return this.currentUserSubject.value?.tipoUsuario === TipoUsuario.CLIENTE;
  }

  isPrestador(): boolean {
    return this.currentUserSubject.value?.tipoUsuario === TipoUsuario.PRESTADOR;
  }

  isAdmin(): boolean {
    return this.currentUserSubject.value?.tipoUsuario === TipoUsuario.ADMIN;
  }

  buscarPerfil(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/perfil`);
  }

  atualizarPerfil(data: UpdatePerfilRequest): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/perfil`, data);
  }

  alterarSenha(data: AlterarSenhaRequest): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/perfil/senha`, data);
  }

  deletarConta(): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/auth/conta`);
  }
}
