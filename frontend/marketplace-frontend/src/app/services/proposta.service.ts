import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Proposta, PageResponse } from '../models/models';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PropostaService {
  private apiUrl = `${environment.apiUrl}/propostas`;

  constructor(private http: HttpClient) {}

  buscarPorId(id: number): Observable<Proposta> {
    return this.http.get<Proposta>(`${this.apiUrl}/${id}`);
  }

  criar(proposta: Proposta): Observable<Proposta> {
    return this.http.post<Proposta>(this.apiUrl, proposta);
  }

  listarPorSolicitacao(solicitacaoId: number, page: number = 0, size: number = 10): Observable<PageResponse<Proposta>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Proposta>>(`${this.apiUrl}/solicitacao/${solicitacaoId}`, { params });
  }

  listarMinhas(page: number = 0, size: number = 10): Observable<PageResponse<Proposta>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<Proposta>>(`${this.apiUrl}/minhas`, { params });
  }

  aceitar(id: number): Observable<Proposta> {
    return this.http.patch<Proposta>(`${this.apiUrl}/${id}/aceitar`, null);
  }

  recusar(id: number): Observable<Proposta> {
    return this.http.patch<Proposta>(`${this.apiUrl}/${id}/recusar`, null);
  }

  cancelar(id: number): Observable<Proposta> {
    return this.http.patch<Proposta>(`${this.apiUrl}/${id}/cancelar`, null);
  }
}
