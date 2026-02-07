import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Prestador, PageResponse } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class PrestadorService {
  private apiUrl = 'http://localhost:8080/api/prestadores';

  constructor(private http: HttpClient) {}

  buscar(
    categoriaId?: number,
    cidade?: string,
    estado?: string,
    minAvaliacao?: number,
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<Prestador>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (categoriaId) params = params.set('categoriaId', categoriaId.toString());
    if (cidade) params = params.set('cidade', cidade);
    if (estado) params = params.set('estado', estado);
    if (minAvaliacao) params = params.set('minAvaliacao', minAvaliacao.toString());

    return this.http.get<PageResponse<Prestador>>(this.apiUrl, { params });
  }

  buscarPorId(id: number): Observable<Prestador> {
    return this.http.get<Prestador>(`${this.apiUrl}/${id}`);
  }

  buscarPorUsuario(usuarioId: number): Observable<Prestador> {
    return this.http.get<Prestador>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  buscarMeuPerfil(): Observable<Prestador> {
    return this.http.get<Prestador>(`${this.apiUrl}/me`);
  }

  criar(prestador: Prestador): Observable<Prestador> {
    return this.http.post<Prestador>(this.apiUrl, prestador);
  }

  atualizar(id: number, prestador: Prestador): Observable<Prestador> {
    return this.http.put<Prestador>(`${this.apiUrl}/${id}`, prestador);
  }

  criarOuAtualizar(prestador: Prestador): Observable<Prestador> {
    return this.http.post<Prestador>(this.apiUrl, prestador);
  }
}
