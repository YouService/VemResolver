import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CompletudePrestador, Prestador, PageResponse } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PrestadorService {
  private apiUrl = `${environment.apiUrl}/prestadores`;

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

  getCompletude(): Observable<CompletudePrestador> {
    return this.http.get<CompletudePrestador>(`${this.apiUrl}/me/completude`);
  }

  buscarProximos(
    lat: number,
    lng: number,
    raioKm: number,
    categoriaId?: number,
    page = 0,
    size = 10
  ): Observable<PageResponse<Prestador>> {
    let params = new HttpParams()
      .set('lat', lat.toString())
      .set('lng', lng.toString())
      .set('raioKm', raioKm.toString())
      .set('page', page.toString())
      .set('size', size.toString());
    if (categoriaId) params = params.set('categoriaId', categoriaId.toString());
    return this.http.get<PageResponse<Prestador>>(`${this.apiUrl}/proximo`, { params });
  }
}
