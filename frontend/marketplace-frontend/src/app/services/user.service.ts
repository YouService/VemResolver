import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  buscarPorId(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  atualizar(id: number, userData: any): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, userData);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarMe(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  atualizarMe(userData: any): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/me`, userData);
  }

  deletarMe(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/me`);
  }
}
