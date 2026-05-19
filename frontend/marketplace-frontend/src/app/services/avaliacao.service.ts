import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Avaliacao, PageResponse } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AvaliacaoService {
  private apiUrl = `${environment.apiUrl}/avaliacoes`;

  constructor(private http: HttpClient) {}

  criar(avaliacao: Avaliacao): Observable<Avaliacao> {
    return this.http.post<Avaliacao>(this.apiUrl, avaliacao);
  }

  criarParaCliente(avaliacao: Avaliacao): Observable<Avaliacao> {
    return this.http.post<Avaliacao>(`${this.apiUrl}/para-cliente`, avaliacao);
  }

  listarPorPrestador(prestadorId: number, page: number = 0, size: number = 10, nota?: number): Observable<PageResponse<Avaliacao>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (nota !== undefined && nota !== null) params = params.set('nota', nota);
    return this.http.get<PageResponse<Avaliacao>>(`${this.apiUrl}/prestador/${prestadorId}`, { params });
  }

  listarPorCliente(clienteId: number, page: number = 0, size: number = 10): Observable<PageResponse<Avaliacao>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<Avaliacao>>(`${this.apiUrl}/cliente/${clienteId}`, { params });
  }

  responder(avaliacaoId: number, resposta: string): Observable<Avaliacao> {
    return this.http.patch<Avaliacao>(`${this.apiUrl}/${avaliacaoId}/resposta`, { resposta });
  }

  editar(avaliacaoId: number, nota: number, comentario: string): Observable<Avaliacao> {
    return this.http.patch<Avaliacao>(`${this.apiUrl}/${avaliacaoId}`, { nota, comentario });
  }
}
