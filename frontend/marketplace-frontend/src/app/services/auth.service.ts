import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, TipoUsuario, User, UpdatePerfilRequest, AlterarSenhaRequest } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
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
        this.currentUserSubject.next(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    localStorage.removeItem('token');
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
    return !!this.getToken();
  }

  isCliente(): boolean {
    return this.currentUserSubject.value?.tipoUsuario === TipoUsuario.CLIENTE;
  }

  isPrestador(): boolean {
    return this.currentUserSubject.value?.tipoUsuario === TipoUsuario.PRESTADOR;
  }

  buscarPerfil(): Observable<User> {
    return this.http.get<User>(`http://localhost:8080/api/perfil`);
  }

  atualizarPerfil(data: UpdatePerfilRequest): Observable<User> {
    return this.http.put<User>(`http://localhost:8080/api/perfil`, data);
  }

  alterarSenha(data: AlterarSenhaRequest): Observable<void> {
    return this.http.put<void>(`http://localhost:8080/api/perfil/senha`, data);
  }

  deletarConta(): Observable<void> {
    return this.http.delete<void>(`http://localhost:8080/api/auth/conta`);
  }
}
